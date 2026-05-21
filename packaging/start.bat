@echo off
REM Thin launcher; main logic is in run.bat (ASCII path name).
call "%~dp0run.bat" %*
exit /b %ERRORLEVEL%
