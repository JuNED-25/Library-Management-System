@echo off
chcp 65001 >nul
title BookHaven - Library Management System

echo ===================================================
echo   Starting BookHaven Library Management System
echo ===================================================

if not exist bin\com\library\Main.class (
    echo [INFO] Binaries not found. Compiling first...
    if not exist bin mkdir bin
    javac --release 17 -encoding UTF-8 -cp "lib/*" -d bin src/com/library/*.java src/com/library/config/*.java src/com/library/model/*.java src/com/library/dao/*.java src/com/library/service/*.java src/com/library/util/*.java src/com/library/ui/*.java
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Compilation failed. Cannot start application.
        pause
        exit /b %ERRORLEVEL%
    )
)

java -cp "bin;lib/*" com.library.Main

pause
