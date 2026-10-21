@echo off
chcp 65001 >nul
title Upload to GitHub - JuNED-25/Library-Management-System

echo ===========================================================================
echo   Uploading BookHaven to https://github.com/JuNED-25/Library-Management-System
echo ===========================================================================
echo.

set "PATH=%USERPROFILE%\.mingit\cmd;%USERPROFILE%\.mingit\mingw64\bin;%PATH%"

git remote remove origin >nul 2>&1
git remote add origin https://github.com/JuNED-25/Library-Management-System.git

echo [INFO] Running: git push -u origin main...
echo [INFO] A GitHub login window will open in your web browser.
echo [INFO] Simply click "Authorize" or sign in to complete the upload.
echo.

git push -u origin main

echo.
if %ERRORLEVEL% EQU 0 (
    echo ===========================================================================
    echo [SUCCESS] Code uploaded successfully to:
    echo           https://github.com/JuNED-25/Library-Management-System
    echo ===========================================================================
) else (
    echo ===========================================================================
    echo [NOTE] If you saw a login prompt, complete it in your browser.
    echo        If it failed, run this file again to retry.
    echo ===========================================================================
)

pause
