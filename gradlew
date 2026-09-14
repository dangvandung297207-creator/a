#!/bin/sh
# True Metallurgy - Gradle launcher.
# Requires a local Gradle 8.10+ installation OR the standard wrapper jar.
# If you have network access, generate the official wrapper once with:
#   gradle wrapper --gradle-version 8.14.2
# then use ./gradlew as usual.
APP_HOME=$(cd "$(dirname "$0")" && pwd)
if [ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
  exec java -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
elif command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "No Gradle installation found and no gradle-wrapper.jar present." >&2
  echo "Install Gradle 8.10+ (https://gradle.org/install/) or run 'gradle wrapper' once." >&2
  exit 1
fi
