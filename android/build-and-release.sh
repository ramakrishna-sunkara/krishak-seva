#!/usr/bin/env bash
# Build debug APK and upload to GitHub Releases
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

VERSION="v1.0.1-debug"
APK_NAME="krishak-seva-debug.apk"
REPO="ramakrishna-sunkara/krishak-seva"

echo "==> Building debug APK..."
./gradlew assembleDebug

mkdir -p dist
cp app/build/outputs/apk/debug/app-debug.apk "dist/${APK_NAME}"
echo "==> APK ready: dist/${APK_NAME} ($(du -h "dist/${APK_NAME}" | cut -f1))"

if ! command -v gh >/dev/null 2>&1; then
  echo "Install GitHub CLI (gh) to upload release automatically."
  echo "Manual upload: GitHub → Releases → upload dist/${APK_NAME}"
  exit 0
fi

echo "==> Creating GitHub Release ${VERSION}..."
gh release upload "${VERSION}" "dist/${APK_NAME}" --repo "${REPO}" --clobber 2>/dev/null || \
gh release create "${VERSION}" \
  "dist/${APK_NAME}" \
  --repo "${REPO}" \
  --title "Krishak Seva v1.0.1 — Debug APK" \
  --notes "Debug build for Hack2skill judges. Install on Android 8+ (API 26). Enable install from unknown sources.

Demo videos: https://ramakrishna-sunkara.github.io/krishak-seva/"

echo ""
echo "Download URL:"
echo "https://github.com/${REPO}/releases/download/${VERSION}/${APK_NAME}"
echo "Or: https://github.com/${REPO}/releases/latest/download/${APK_NAME}"
