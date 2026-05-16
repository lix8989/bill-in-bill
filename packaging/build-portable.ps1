#!/usr/bin/env pwsh
# Assembles ZIP portable Windows distribution under repo ./dist-portable/
# Prerequisites on build machine: JDK 21 + Maven + Node/npm
# Optional: portable JDK folder to bundle as .\jdk-21\ (argument -JdkPath)

param(
  [string]$JdkPath = "",
  [switch]$SkipNpmCi,
  [switch]$KeepStaging
)

$ErrorActionPreference = "Stop"

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Frontend = Join-Path $RepoRoot "frontend"
$Backend = Join-Path $RepoRoot "backend"
$OutRoot = Join-Path $RepoRoot "dist-portable"
$StagingRoot = Join-Path $OutRoot "_staging"
$BillInBill = Join-Path $StagingRoot "BillInBill"

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

Write-Host "[1/6] Frontend build ..." -ForegroundColor Cyan
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

Write-Host "[2/6] Maven package backend (embeds frontend dist) ..." -ForegroundColor Cyan
Push-Location $Backend
try {
  mvn -q -DskipTests package
}
finally {
  Pop-Location
}

Write-Host "[3/6] Stage portable layout ..." -ForegroundColor Cyan
if (Test-Path -LiteralPath $StagingRoot) {
  Remove-Item -LiteralPath $StagingRoot -Recurse -Force
}
New-Item -ItemType Directory -Path (Join-Path $BillInBill "lib") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $BillInBill "data") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $BillInBill "config") -Force | Out-Null

$targetDir = Join-Path $Backend "target"
$fatJar = Get-ChildItem $targetDir -Filter "*.jar" |
  Where-Object {
    $_.Name -match '^bill-in-bill-backend-[0-9].*\.jar$' -and
    $_.Name -notmatch 'plain|sources|javadoc'
  } |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1

if (-not $fatJar) {
  throw "Fat JAR not found under backend/target/. Run mvn package and ensure frontend/dist exists."
}

Copy-Item -LiteralPath $fatJar.FullName -Destination (Join-Path $BillInBill "lib\$JarDestName") -Force
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "start.bat") -Destination (Join-Path $BillInBill "start.bat") -Force

$readmeSrc = Join-Path $PSScriptRoot "templates\README-PORTABLE.txt"
Copy-Item -LiteralPath $readmeSrc -Destination (Join-Path $BillInBill "README.txt") -Force

$dataKeep = Join-Path $PSScriptRoot "templates\data\.keep"
if (Test-Path -LiteralPath $dataKeep) {
  Copy-Item -LiteralPath $dataKeep -Destination (Join-Path $BillInBill "data\.keep") -Force
}

$configExampleSrc = Join-Path $PSScriptRoot "config\application.properties.example"
if (Test-Path -LiteralPath $configExampleSrc) {
  Copy-Item -LiteralPath $configExampleSrc -Destination (
    Join-Path $BillInBill "config\application.properties.example") -Force
}

if (-not [string]::IsNullOrWhiteSpace($JdkPath)) {
  Write-Host "[4/6] Copy portable JDK 21 ..." -ForegroundColor Cyan
  $jdkResolved = (Resolve-Path -LiteralPath $JdkPath).Path
  $jdkJava = Join-Path $jdkResolved "bin\java.exe"
  if (-not (Test-Path -LiteralPath $jdkJava)) {
    throw "JdkPath must point at JDK/JRE root with bin/java.exe: $jdkResolved"
  }
  $jdkDest = Join-Path $BillInBill "jdk-21"
  robocopy $jdkResolved $jdkDest /E /NFL /NDL /NJH /NJS /NP | Out-Null
  if ($LASTEXITCODE -gt 8) {
    throw "robocopy failed with exit code $LASTEXITCODE"
  }
}
else {
  Write-Host "[4/6] Skip bundling JDK (pass -JdkPath to embed jdk-21) ..." -ForegroundColor Yellow
}

Write-Host "[5/6] Create ZIP archive ..." -ForegroundColor Cyan
if (-not (Test-Path -LiteralPath $OutRoot)) {
  New-Item -ItemType Directory -Path $OutRoot | Out-Null
}

$ZipName = "bill-in-bill-$version-windows-amd64.zip"
$ZipPath = Join-Path $OutRoot $ZipName
if (Test-Path -LiteralPath $ZipPath) {
  Remove-Item -LiteralPath $ZipPath -Force
}

Compress-Archive -Path $BillInBill -DestinationPath $ZipPath -Force

if (-not $KeepStaging) {
  Remove-Item -LiteralPath $StagingRoot -Recurse -Force
}

Write-Host "[6/6] Done: $ZipPath" -ForegroundColor Green
