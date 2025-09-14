#!/usr/bin/env bash
set -euo pipefail

# Usage: scripts/prepare-standalone-war.sh /absolute/path/to/dmtools-flutter-main-app-*.zip

ZIP_PATH=${1:-}
if [[ -z "${ZIP_PATH}" ]]; then
  echo "ERROR: ZIP path is required.\nUsage: $0 /absolute/path/to/dmtools-flutter-main-app.zip" >&2
  exit 1
fi

if [[ ! -f "${ZIP_PATH}" ]]; then
  echo "ERROR: File not found: ${ZIP_PATH}" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SERVER_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${SERVER_DIR}/.." && pwd)"

echo "ZIP: ${ZIP_PATH}"
echo "Server dir: ${SERVER_DIR}"
echo "Repo root: ${REPO_ROOT}"

echo "Building server bootJar..."
pushd "${REPO_ROOT}" >/dev/null
./gradlew :dmtools-server:bootJar --no-daemon --info >/dev/null
popd >/dev/null

BOOT_JAR="${REPO_ROOT}/dmtools-appengine.jar"
if [[ ! -f "${BOOT_JAR}" ]]; then
  echo "ERROR: Expected boot jar not found: ${BOOT_JAR}" >&2
  exit 1
fi

STAGE_DIR="${SERVER_DIR}/build/standalone"
TMP_ADD_DIR="${STAGE_DIR}/add"
SPA_DIR="${STAGE_DIR}/spa"

echo "Preparing staging directories at ${STAGE_DIR}..."
rm -rf "${STAGE_DIR}"
mkdir -p "${TMP_ADD_DIR}" "${SPA_DIR}"

echo "Unpacking SPA zip..."
unzip -q "${ZIP_PATH}" -d "${SPA_DIR}"

# Clean MacOS resource forks and __MACOSX artifacts
find "${SPA_DIR}" -name "__MACOSX" -type d -prune -exec rm -rf {} + || true
find "${SPA_DIR}" -type f -name "._*" -delete || true

# Detect SPA root by locating index.html
SPA_INDEX_PATH="$(find "${SPA_DIR}" -type f -name "index.html" | head -n1 || true)"
if [[ -z "${SPA_INDEX_PATH}" ]]; then
  echo "ERROR: Could not locate index.html in extracted SPA. Checked under ${SPA_DIR}" >&2
  exit 1
fi
SPA_ROOT="$(dirname "${SPA_INDEX_PATH}")"

echo "Preparing jar update payload..."
mkdir -p "${TMP_ADD_DIR}/BOOT-INF/classes/static"
rsync -a --delete "${SPA_ROOT}/" "${TMP_ADD_DIR}/BOOT-INF/classes/static/"

# Force standalone profile by overlaying application.properties inside the artifact
mkdir -p "${TMP_ADD_DIR}/BOOT-INF/classes"
cat > "${TMP_ADD_DIR}/BOOT-INF/classes/application.properties" <<'EOF'
# Auto-activated by standalone distribution packaging
spring.profiles.active=standalone
EOF

echo "Updating boot jar in-place with SPA assets and standalone profile activation..."
cp -f "${BOOT_JAR}" "${STAGE_DIR}/dmtools-standalone.jar"
pushd "${TMP_ADD_DIR}" >/dev/null
zip -qr ../dmtools-standalone.jar BOOT-INF
popd >/dev/null

cp -f "${STAGE_DIR}/dmtools-standalone.jar" "${STAGE_DIR}/dmtools-standalone.war"

echo "Standalone artifacts prepared:"
echo "  Jar: ${STAGE_DIR}/dmtools-standalone.jar"
echo "  War: ${STAGE_DIR}/dmtools-standalone.war"
echo "Run:   java -jar ${STAGE_DIR}/dmtools-standalone.jar"


