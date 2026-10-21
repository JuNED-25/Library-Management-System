@echo off
chcp 65001 >nul
echo ===================================================
echo   Pushing BookHaven to GitHub (JuNED-25)
echo ===================================================

git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Pushed successfully to https://github.com/JuNED-25/Library-Management-System
) else (
    echo [INFO] If prompted, sign in to your GitHub account to authorize the upload.
)

pause
