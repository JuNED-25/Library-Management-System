# PowerShell Launcher for BookHaven Console Application
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$Host.UI.RawUI.WindowTitle = "BookHaven - Library Management System"

Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "  Starting BookHaven Library Management System     " -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin\com\library\Main.class")) {
    Write-Host "[INFO] Binaries not found. Compiling sources..." -ForegroundColor Yellow
    if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
    javac --release 17 -encoding UTF-8 -cp "lib/*" -d bin src/com/library/*.java src/com/library/config/*.java src/com/library/model/*.java src/com/library/dao/*.java src/com/library/service/*.java src/com/library/util/*.java src/com/library/ui/*.java
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
        Exit $LASTEXITCODE
    }
}

java -cp "bin;lib/*" com.library.Main
