#!/usr/bin/env bash
set -euo pipefail
GRADLE_VERSION=9.2.1
TOOLS_DIR="$(cd "$(dirname "$0")" && pwd)/.gradle-tools"
GRADLE_HOME_LOCAL="$TOOLS_DIR/gradle-$GRADLE_VERSION"
mkdir -p "$TOOLS_DIR"
if [[ ! -x "$GRADLE_HOME_LOCAL/bin/gradle" ]]; then
  command -v curl >/dev/null || { echo "ERROR: curl is required"; exit 1; }
  command -v unzip >/dev/null || { echo "ERROR: unzip is required"; exit 1; }
  curl -fL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$TOOLS_DIR/gradle-${GRADLE_VERSION}-bin.zip"
  unzip -oq "$TOOLS_DIR/gradle-${GRADLE_VERSION}-bin.zip" -d "$TOOLS_DIR"
fi
"$GRADLE_HOME_LOCAL/bin/gradle" --version
"$GRADLE_HOME_LOCAL/bin/gradle" build --refresh-dependencies
