#!/usr/bin/env pwsh
# Builds bill-in-bill-{version}-windows-amd64.zip into repo ./dist-portable/
#
# Prerequisites: JDK 21, Maven, Node/npm on PATH
# Runtime in zip: bundles jdk-21\ from -JdkPath, else env JDK_PACK_PATH, else JAVA_HOME (must contain bin/java.exe).
# Opt out: -SkipJdkBundle (zip then requires Java 21 on PATH on the user's PC).

param(
  [string]$JdkPath = "",
  [switch]$SkipNpmCi,
  [switch]$KeepStaging,
  [switch]$SkipJdkBundle
)

$ErrorActionPreference = "Stop"

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Frontend = Join-Path $RepoRoot "frontend"
$Backend = Join-Path $RepoRoot "backend"
$OutRoot = Join-Path $RepoRoot "dist-portable"
$StagingRoot = Join-Path $OutRoot "_staging"
# Root folder inside ZIP (ASCII for maximum Windows / tooling compatibility).
$ProductRootFolder = "BillInBill"

$runBat = Join-Path $PSScriptRoot "run.bat"
if (-not (Test-Path -LiteralPath $runBat)) {
  throw "Missing packaging/run.bat (main launcher)."
}

$pomXml = [xml](Get-Content -Raw (Join-Path $Backend "pom.xml"))
if (-not $pomXml.project.version) {
  throw "backend/pom.xml missing <version>."
}
$version = $pomXml.project.version.Trim()

$JarDestName = "bill-in-bill-backend.jar"
function Assert-PathExists {
  param ([string]$Path, [string]$Message)
  if (-not (Test-Path -LiteralPath $Path)) { throw $Message }
}

function Invoke-PortableSeedDb {
  param(
    [string]$BackendRoot,
    [string]$DbFilePath,
    [string]$SchemaPath
  )
  Write-Host "       generating data\wechat-bill.db from schema.sql ..." -ForegroundColor DarkGray
  $cpFile = [System.IO.Path]::ChangeExtension([System.IO.Path]::GetTempFileName(), "cp.txt")
  try {
    Push-Location $BackendRoot
    mvn -q -DskipTests compile dependency:build-classpath "-Dmdep.outputFile=$cpFile"
    if (-not $?) {
      throw "mvn dependency:build-classpath failed."
    }
  }
  finally {
    Pop-Location
  }

  Assert-PathExists $cpFile "Could not resolve Maven classpath for SQLite seed."

  try {
    $deps = (Get-Content -LiteralPath $cpFile -Raw).Trim()
    Remove-Item -LiteralPath $cpFile -Force -ErrorAction SilentlyContinue

    $classesDir = Join-Path $BackendRoot "target\classes"
    $classpath = "$classesDir;$deps"

    $dbDir = Split-Path -Parent $DbFilePath
    if (-not (Test-Path $dbDir)) {
      New-Item -ItemType Directory -Force -Path $dbDir | Out-Null
    }

    java -cp $classpath com.lex.wechatbill.tools.PortableSeedDb $DbFilePath $SchemaPath
    if (-not $?) {
      throw "PortableSeedDb failed."
    }
  }
  finally {
    if (Test-Path -LiteralPath $cpFile) {
      Remove-Item -LiteralPath $cpFile -Force -ErrorAction SilentlyContinue
    }
  }
}

Write-Host "[1/7] Frontend build ..." -ForegroundColor Cyan
Push-Location $Frontend
try {
  if (-not $SkipNpmCi) {
    npm ci
  }
  npm run build
}
finally {
  Pop-Location
}

Assert-PathExists (Join-Path $Frontend "dist\index.html") "frontend/dist/index.html missing after npm run build."

Write-Host "[2/7] Maven package backend (embed frontend dist into JAR) ..." -ForegroundColor Cyan
Push-Location $Backend
try {
  mvn -q -DskipTests package
}
finally {
  Pop-Location
}

Write-Host "[3/7] Prepare staging folder `"$ProductRootFolder`" ..." -ForegroundColor Cyan
if (Test-Path -LiteralPath $StagingRoot) {
  Remove-Item -LiteralPath $StagingRoot -Recurse -Force
}
$ProdRoot = Join-Path $StagingRoot $ProductRootFolder
New-Item -ItemType Directory -Path (Join-Path $ProdRoot "lib") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $ProdRoot "data") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $ProdRoot "config") -Force | Out-Null

$targetDir = Join-Path $Backend "target"
$fatJar = Get-ChildItem $targetDir -Filter "*.jar" |
  Where-Object {
    $_.Name -match '^bill-in-bill-backend-[0-9].*\.jar$' -and
    $_.Name -notmatch 'plain|sources|javadoc'
  } |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1

if (-not $fatJar) {
  throw "Fat JAR not found under backend/target/."
}

Write-Host "[4/7] Copy runtime files ..." -ForegroundColor Cyan
Copy-Item -LiteralPath $fatJar.FullName -Destination (Join-Path $ProdRoot "lib\$JarDestName") -Force

Copy-Item -LiteralPath $runBat -Destination (Join-Path $ProdRoot "run.bat") -Force

Copy-Item -LiteralPath (Join-Path $PSScriptRoot "start.bat") -Destination (Join-Path $ProdRoot "start.bat") -Force

$readmeSrc = Join-Path $PSScriptRoot "templates\README-PORTABLE.txt"
Copy-Item -LiteralPath $readmeSrc -Destination (Join-Path $ProdRoot "README.txt") -Force

$configProps = Join-Path $PSScriptRoot "templates\config\application.properties"
Assert-PathExists $configProps "Missing packaging/templates/config/application.properties."
Copy-Item -LiteralPath $configProps -Destination (Join-Path $ProdRoot "config\application.properties") -Force

$destDb = Join-Path $ProdRoot "data\wechat-bill.db"
$repoDb = Join-Path $RepoRoot "data\wechat-bill.db"
$schemaSql = Join-Path $Backend "src\main\resources\db\schema.sql"

Assert-PathExists $schemaSql "Missing backend schema at $schemaSql"

Write-Host "[5/7] SQLite database bundle ..." -ForegroundColor Cyan
if (Test-Path -LiteralPath $repoDb) {
  Copy-Item -LiteralPath $repoDb -Destination $destDb -Force
  Write-Host "       used repository data\\wechat-bill.db as shipped database" -ForegroundColor DarkGray
}
else {
  Invoke-PortableSeedDb -BackendRoot $Backend -DbFilePath $destDb -SchemaPath $schemaSql
}

if ($SkipJdkBundle) {
  Write-Host "[6/7] Skip bundling JDK (-SkipJdkBundle); end users need Java 21 on PATH" -ForegroundColor Yellow
}
else {
  Write-Host "[6/7] Copy portable JDK -> jdk-21 ..." -ForegroundColor Cyan
  $jdkResolved = $null
  if (-not [string]::IsNullOrWhiteSpace($JdkPath)) {
    $jdkResolved = (Resolve-Path -LiteralPath $JdkPath).Path
  }
  elseif (-not [string]::IsNullOrWhiteSpace($env:JDK_PACK_PATH)) {
    $p = Join-Path $env:JDK_PACK_PATH.Trim() "bin\java.exe"
    if (-not (Test-Path -LiteralPath $p)) { throw "JDK_PACK_PATH is set but missing bin/java.exe: $($env:JDK_PACK_PATH.Trim())" }
    $jdkResolved = (Resolve-Path -LiteralPath $env:JDK_PACK_PATH.Trim()).Path
  }
  elseif (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    $p = Join-Path $env:JAVA_HOME.Trim() "bin\java.exe"
    if (-not (Test-Path -LiteralPath $p)) { throw "JAVA_HOME is set but missing bin/java.exe: $($env:JAVA_HOME.Trim())" }
    $jdkResolved = (Resolve-Path -LiteralPath $env:JAVA_HOME.Trim()).Path
  }
  if (-not $jdkResolved) {
    throw "Could not locate a JDK/JRE to bundle. Set JAVA_HOME or JDK_PACK_PATH, pass -JdkPath `"...`", or use -SkipJdkBundle."
  }
  $jdkJavaExe = Join-Path $jdkResolved "bin\java.exe"
  if (-not (Test-Path -LiteralPath $jdkJavaExe)) {
    throw "JDK root must contain bin\java.exe: $jdkResolved"
  }
  $jdkDest = Join-Path $ProdRoot "jdk-21"
  robocopy $jdkResolved $jdkDest /E /NFL /NDL /NJH /NJS /NP | Out-Null
  if ($LASTEXITCODE -gt 8) {
    throw "robocopy failed with exit code $LASTEXITCODE"
  }
}

Write-Host "[7/7] Zip archive -> dist-portable ..." -ForegroundColor Cyan
if (-not (Test-Path -LiteralPath $OutRoot)) {
  New-Item -ItemType Directory -Path $OutRoot | Out-Null
}

$ZipName = "bill-in-bill-$version-windows-amd64.zip"
$ZipPath = Join-Path $OutRoot $ZipName
if (Test-Path -LiteralPath $ZipPath) {
  Remove-Item -LiteralPath $ZipPath -Force
}

Compress-Archive -Path $ProdRoot -DestinationPath $ZipPath -Force

if (-not $KeepStaging) {
  Remove-Item -LiteralPath $StagingRoot -Recurse -Force
}

Write-Host ""
Write-Host "Done: $ZipPath" -ForegroundColor Green
