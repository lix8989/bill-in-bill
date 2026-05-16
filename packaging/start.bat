@echo off
setlocal ENABLEDELAYEDEXPANSION
cd /d "%~dp0"
set "_JAVA_EXE="
if exist "%~dp0jdk-21\bin\java.exe" set "_JAVA_EXE=%~dp0jdk-21\bin\java.exe"
if not defined _JAVA_EXE (
  where java >nul 2>&1
  if errorlevel 1 (
    echo [帐里说帐] 未检测到 Java。
    echo 请将 JDK 21 便携版解压到与本脚本同目录的 jdk-21\ 文件夹，或在系统中安装 JDK 21 并加入 PATH。
    pause
    exit /b 1
  )
  set "_JAVA_EXE=java"
)
echo ========================================
echo 帐里说帐 正在启动……
echo 启动完成后在浏览器访问: http://127.0.0.1:8080
echo SQLite 数据库文件: .\data\wechat-bill.db
echo 关闭本窗口即可结束服务。
echo ========================================
"%_JAVA_EXE%" -Dfile.encoding=UTF-8 %JAVA_OPTS% -jar "%~dp0lib\bill-in-bill-backend.jar"
set "EXITCODE=!errorlevel!"
if not "!EXITCODE!"=="0" (
  echo 进程异常退出，错误代码 !EXITCODE!
  pause
  exit /b !EXITCODE!
)
endlocal
exit /b 0
