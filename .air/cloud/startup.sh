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

# --- Mono (msbuild/xbuild for nuget-extensions) -------------------------------
if ! command -v xbuild >/dev/null 2>&1; then
  log "Installing mono-devel and mono-xbuild (needed to build .NET nuget-extensions)"
  sudo apt-get update -qq
  sudo apt-get install -y -qq mono-devel mono-xbuild \
    || log "WARNING: mono install failed; .NET extension builds will not work"
fi

# --- JVM proxy settings -------------------------------------------------------
# The JVM (Gradle wrapper, daemon, forked test JVMs) ignores http(s)_proxy env
# vars, so derive JVM proxy properties from the current environment on every
# boot (the proxy address may differ between runs).
JAVA_PROXY_OPTS=""
PROXY_URL="${https_proxy:-${HTTPS_PROXY:-}}"
if [ -n "$PROXY_URL" ]; then
  PROXY_HOST=$(echo "$PROXY_URL" | sed -E 's#^[a-zA-Z]+://##; s#/.*##; s#:[0-9]+$##')
  PROXY_PORT=$(echo "$PROXY_URL" | sed -E 's#.*:([0-9]+)/?$#\1#')
  JAVA_PROXY_OPTS="-Dhttp.proxyHost=$PROXY_HOST -Dhttp.proxyPort=$PROXY_PORT -Dhttps.proxyHost=$PROXY_HOST -Dhttps.proxyPort=$PROXY_PORT -Dhttp.nonProxyHosts=localhost|127.0.0.1"
  log "Using HTTP proxy $PROXY_HOST:$PROXY_PORT for JVM processes"
fi

# --- Login-shell environment -------------------------------------------------
cat > "$ENV_FILE" <<EOF
export JAVA_HOME="$JAVA17_HOME"
export PATH="$JAVA17_HOME/bin:\$PATH"
EOF
if [ -n "$JAVA_PROXY_OPTS" ]; then
  printf 'export JAVA_TOOL_OPTIONS="%s"\n' "$JAVA_PROXY_OPTS" >> "$ENV_FILE"
fi
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
if [ -n "$JAVA_PROXY_OPTS" ]; then
  export JAVA_TOOL_OPTIONS="$JAVA_PROXY_OPTS"
fi

cd "$REPO_DIR"
chmod +x ./gradlew

# --- TeamCity distribution (non-public compile dependencies) -----------------
# The build resolves org.jetbrains.teamcity[.internal] artifacts from a locally
# installed TeamCity at nuget-server/teamcity/environments/<version> (see the
# flatDir repository in build.gradle). The rodm install task downloads the wrong
# default version (9.0), so fetch the distribution matching libs.versions.toml
# directly.
TC_VERSION=$(sed -n 's/^teamcity *= *"\(.*\)"/\1/p' gradle/libs.versions.toml)
TC_HOME="nuget-server/teamcity/environments/${TC_VERSION}"
TC_LIB_DIR="$TC_HOME/webapps/ROOT/WEB-INF/lib"
if [ ! -d "$TC_LIB_DIR" ]; then
  log "Downloading TeamCity ${TC_VERSION} distribution (~1.5 GB, one-time)"
  curl -fSL --retry 3 "https://download.jetbrains.com/teamcity/TeamCity-${TC_VERSION}.tar.gz" -o /tmp/TeamCity.tar.gz
  log "Unpacking TeamCity ${TC_VERSION}"
  rm -rf /tmp/tc-extract "$TC_HOME"
  mkdir -p /tmp/tc-extract "$(dirname "$TC_HOME")"
  tar -xzf /tmp/TeamCity.tar.gz -C /tmp/tc-extract
  mv /tmp/tc-extract/TeamCity "$TC_HOME"
  rm -rf /tmp/TeamCity.tar.gz /tmp/tc-extract
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
