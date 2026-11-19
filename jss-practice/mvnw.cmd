@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows (Auto-bootstrapping)
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "DIR=%~dp0"
set "MAVEN_VERSION=3.9.9"
set "MAVEN_HOME=%DIR%.mvn\wrapper\apache-maven-%MAVEN_VERSION%"
set "MAVEN_EXE=%MAVEN_HOME%\bin\mvn.cmd"

if exist "%MAVEN_EXE%" (
    goto :runMaven
)

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set "MAVEN_EXE=mvn"
    goto :runMaven
)

echo [INFO] Apache Maven not found locally. Downloading Apache Maven %MAVEN_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference = 'Stop';" ^
    "$zipPath = Join-Path '%DIR%.mvn\wrapper' 'maven.zip';" ^
    "$extractPath = '%DIR%.mvn\wrapper';" ^
    "if (-not (Test-Path $extractPath)) { New-Item -ItemType Directory -Path $extractPath -Force | Out-Null };" ^
    "$url = 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip';" ^
    "Write-Host '[INFO] Downloading from' $url '...';" ^
    "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
    "(New-Object System.Net.WebClient).DownloadFile($url, $zipPath);" ^
    "Write-Host '[INFO] Extracting Maven zip...';" ^
    "Expand-Archive -Path $zipPath -DestinationPath $extractPath -Force;" ^
    "Remove-Item $zipPath -Force;" ^
    "Write-Host '[INFO] Maven %MAVEN_VERSION% ready!'"

if not exist "%MAVEN_EXE%" (
    echo [ERROR] Failed to download or locate Apache Maven. Please install Maven manually or ensure internet connectivity.
    exit /b 1
)

:runMaven
"%MAVEN_EXE%" %*
exit /b %ERRORLEVEL%
