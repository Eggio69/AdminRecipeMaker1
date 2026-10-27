@echo off
setlocal
where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java was not found. Install JDK 21 and set JAVA_HOME.
  pause
  exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo ERROR: JAVA_HOME must point to JDK 21.
  echo Current JAVA_HOME: %JAVA_HOME%
  pause
  exit /b 1
)
set "GRADLE_VERSION=9.2.1"
set "TOOLS_DIR=%~dp0.gradle-tools"
set "GRADLE_HOME_LOCAL=%TOOLS_DIR%\gradle-%GRADLE_VERSION%"
set "ZIP=%TOOLS_DIR%\gradle-%GRADLE_VERSION%-bin.zip"
if not exist "%GRADLE_HOME_LOCAL%\bin\gradle.bat" (
  if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'"
  if errorlevel 1 (
    echo ERROR: Could not download Gradle. Check internet access and try again.
    pause
    exit /b 1
  )
  echo Extracting Gradle...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%TOOLS_DIR%' -Force"
  if errorlevel 1 (
    echo ERROR: Could not extract Gradle.
    pause
    exit /b 1
  )
)
call "%GRADLE_HOME_LOCAL%\bin\gradle.bat" --version
if errorlevel 1 exit /b 1
call "%GRADLE_HOME_LOCAL%\bin\gradle.bat" build --refresh-dependencies
set "RESULT=%ERRORLEVEL%"
if "%RESULT%"=="0" echo Build successful. Check build\libs\ for the JAR.
if not "%RESULT%"=="0" echo Build failed. Copy the full error output for troubleshooting.
pause
exit /b %RESULT%
