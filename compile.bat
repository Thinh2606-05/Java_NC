@echo off
chcp 65001 >nul
echo ========================================================
echo Compiling TFUS Java Project (JDK 17+ Standard)
echo ========================================================

if not exist "bin" mkdir bin
powershell -Command "$files = Get-ChildItem -Path 'src\main\java' -Filter '*.java' -Recurse | ForEach-Object { $_.FullName }; [System.IO.File]::WriteAllLines('sources.txt', $files); javac -d 'bin' '@sources.txt'; $res = $?; Remove-Item 'sources.txt'; if ($res) { Write-Host 'Compilation SUCCESSFUL!' -ForegroundColor Green } else { Write-Host 'Compilation FAILED!' -ForegroundColor Red; exit 1 }"
