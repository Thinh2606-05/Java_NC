@echo off
chcp 65001 >nul
if not exist "bin\tfus\app\WebMain.class" (
    echo Building project first...
    call compile.bat
)
echo Starting TFUS Pure Java Web Server...
java -cp "bin" tfus.app.WebMain 8080
