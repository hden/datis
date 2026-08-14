#!/bin/sh
set -eu

version=2026.05.25
target=.tools/clj-kondo
binary="$target/clj-kondo"

if [ -x "$binary" ]; then
  exit 0
fi

case "$(uname -s)-$(uname -m)" in
  Darwin-arm64) platform=macos-aarch64 ;;
  Darwin-x86_64) platform=macos-amd64 ;;
  Linux-aarch64) platform=linux-aarch64 ;;
  Linux-x86_64) platform=linux-amd64 ;;
  *)
    echo "Unsupported platform for clj-kondo: $(uname -s)-$(uname -m)" >&2
    exit 1
    ;;
esac

tmpdir=$(mktemp -d)
trap 'rm -rf "$tmpdir"' EXIT HUP INT TERM

archive="$tmpdir/clj-kondo.zip"
url="https://github.com/clj-kondo/clj-kondo/releases/download/v$version/clj-kondo-$version-$platform.zip"

mkdir -p "$target"
curl --fail --location --silent --show-error --output "$archive" "$url"
unzip -q "$archive" -d "$target"
chmod +x "$binary"
