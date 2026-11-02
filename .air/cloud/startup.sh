#!/usr/bin/env bash
# Air cloud startup script for teamcity-nuget-support.
#
# Prepares a working Gradle build of the TeamCity NuGet support plugin:
#   - JDK 17 (Gradle 8.14 cannot run on very new JDKs shipped in the base image)
#   - Mono (msbuild) for the .NET nuget-extensions
#   - TeamCity 2025.07 distribution (provides non-public compile dependencies)
#   - Primed Gradle caches and an assembled plugin as the build smoke test
set -euo pipefail

if [ "${AIR_STARTUP_MODE:-}" = warmup ]; then WARMUP=1; else WARMUP=; fi

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="$HOME/.air-env.sh"

log() { echo "[startup] $(date -u +%H:%M:%S) $*"; }

# --- JDK 17 -----------------------------------------------------------------
install_jdk() {
  if [ -x /usr/lib/jvm/temurin-17-jdk-amd64/bin/java ]; then
    JAVA17_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
    return
  fi
  JAVA17_HOME=$(ls -d /usr/lib/jvm/*17* 2>/dev/null | head -1 || true)
  if [ -z "$JAVA17_HOME" ]; then
    log "Installing OpenJDK 17"
    sudo apt-get update -qq
    sudo apt-get install -y -qq openjdk-17-jdk-headless
    JAVA17_HOME=$(ls -d /usr/lib/jvm/*17* | head -1)
  fi
}
install_jdk
log "Using JDK at $JAVA17_HOME"

# --- Mono (msbuild for nuget-extensions) ------------------------------------
if ! command -v msbuild >/dev/null 2>&1 && ! command -v mono >/dev/null 2>&1; then
  log "Installing mono-devel (needed to build .NET nuget-extensions)"
  sudo apt-get update -qq
  sudo apt-get install -y -qq mono-devel || log "WARNING: mono-devel install failed; .NET extension builds will not work"
fi

# --- Login-shell environment -------------------------------------------------
cat > "$ENV_FILE" <<EOF
export JAVA_HOME="$JAVA17_HOME"
export PATH="$JAVA17_HOME/bin:\$PATH"
EOF
MARKER="# air-env"
add_source_line() {
  local f="$1"
  grep -qF "$MARKER" "$f" 2>/dev/null || printf '\n%s\n. "%s"\n' "$MARKER" "$ENV_FILE" >> "$f"
}
for f in "$HOME/.bash_profile" "$HOME/.bash_login" "$HOME/.profile"; do
  if [ -f "$f" ]; then add_source_line "$f"; PROFILE_DONE=1; break; fi
done
[ -n "${PROFILE_DONE:-}" ] || add_source_line "$HOME/.profile"
add_source_line "$HOME/.bashrc"

export JAVA_HOME="$JAVA17_HOME"
export PATH="$JAVA17_HOME/bin:$PATH"

cd "$REPO_DIR"
chmod +x ./gradlew

# --- TeamCity distribution (non-public compile dependencies) -----------------
TC_VERSION=$(sed -n 's/^teamcity *= *"\(.*\)"/\1/p' gradle/libs.versions.toml)
TC_LIB_DIR="nuget-server/teamcity/environments/${TC_VERSION}/webapps/ROOT/WEB-INF/lib"
if [ ! -d "$TC_LIB_DIR" ]; then
  log "Downloading TeamCity ${TC_VERSION} distribution (several GB, one-time)"
  ./gradlew --no-daemon ":nuget-server:installTeamCity${TC_VERSION}"
  log "TeamCity ${TC_VERSION} installed"
else
  log "TeamCity ${TC_VERSION} already installed"
fi

# --- Prime Gradle caches / assemble the plugin -------------------------------
if [ -n "$WARMUP" ]; then
  log "Warmup: assembling the plugin to prime Gradle caches"
  ./gradlew --no-daemon :nuget-server:assemble
  log "Warmup assemble finished"
fi

# --- Healthcheck --------------------------------------------------------------
healthcheck() {
  log "Healthcheck: verifying JDK and plugin build"
  java -version 2>&1 | grep -q 'version "17' || { log "Healthcheck FAILED: JDK 17 not active"; return 1; }
  [ -d "$TC_LIB_DIR" ] || { log "Healthcheck FAILED: TeamCity libs missing at $TC_LIB_DIR"; return 1; }
  ./gradlew --no-daemon :nuget-server:assemble || { log "Healthcheck FAILED: gradle assemble failed"; return 1; }
  ls nuget-server/build/distributions/dotNetPackagesSupport.zip >/dev/null 2>&1 \
    || { log "Healthcheck FAILED: plugin zip not produced"; return 1; }
  log "Healthcheck OK"
}

if [ -n "$WARMUP" ]; then
  healthcheck
fi

log "Startup complete"
