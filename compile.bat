@echo off
chcp 65001 >nul
echo ===================================================
echo   Compiling BookHaven Library Management System
echo ===================================================

if not exist bin mkdir bin

javac --release 17 -encoding UTF-8 -cp "lib/*" -d bin src/com/library/*.java src/com/library/config/*.java src/com/library/model/*.java src/com/library/dao/*.java src/com/library/service/*.java src/com/library/util/*.java src/com/library/ui/*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation finished successfully! Class files written to bin/
) else (
    echo [ERROR] Compilation failed. Please check the error messages above.
)

pause
