@rem True Metallurgy - Gradle launcher (Windows).
@echo off
set APP_HOME=%~dp0
if exist "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" (
  java -jar "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" %*
) else (
  where gradle >nul 2>nul
  if %ERRORLEVEL% equ 0 (
    gradle %*
  ) else (
    echo No Gradle installation found and no gradle-wrapper.jar present.
    echo Install Gradle 8.10+ (https://gradle.org/install/) or run 'gradle wrapper' once.
    exit /b 1
  )
)
