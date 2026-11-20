@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows (Auto-bootstrapping)
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "STS_JAVA17=C:\Assessment_Workbench\spring-tool-suite\sts-4.16.0.RELEASE\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.4.v20220903-1038\jre"
if not defined JAVA_HOME (
    if exist "%STS_JAVA17%\bin\java.exe" (
        set "JAVA_HOME=%STS_JAVA17%"
        set "PATH=%STS_JAVA17%\bin;%PATH%"
    )
) else (
    echo %JAVA_HOME% | findstr /i "jdk-25" >nul
    if not errorlevel 1 (
        if exist "%STS_JAVA17%\bin\java.exe" (
            set "JAVA_HOME=%STS_JAVA17%"
            set "PATH=%STS_JAVA17%\bin;%PATH%"
        )
    )
)

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
