@echo off
chcp 65001 >nul
if not exist "bin\tfus\app\CliMain.class" (
    echo Building project first...
    call compile.bat
)
java -cp "bin" tfus.app.CliMain %*
