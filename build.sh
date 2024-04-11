#!/usr/bin/env bash

set -e

if $(sed --version 2>/dev/null | grep -q GNU); then
  echo "Use GNU sed"
  SED_INPLACE="sed -i"
else
  echo "Use BSD sed"
  SED_INPLACE="sed -i .bak"
fi


DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $DIR

echo "Building Go library"
cd golib
mkdir -p output
gomobile bind -target=android -androidapi=21 -o output/golib.aar

echo "Extracting Go library sources and assets"
cd output
rm -rf "golib" "golib-sources"
unzip golib.aar -d golib > /dev/null
unzip golib-sources.jar -d golib-sources > /dev/null

echo "Copying Go library sources and assets to Android app"
rm -rf "$DIR/androidapp/app/src/main/java/go" "$DIR/androidapp/app/src/main/java/golib" "$DIR/androidapp/app/src/main/assets/golib"
cp -r golib-sources/go "$DIR/androidapp/app/src/main/java"
cp -r golib-sources/golib "$DIR/androidapp/app/src/main/java"
$SED_INPLACE 's/System.loadLibrary/\/\//g'  "$DIR/androidapp/app/src/main/java/go/Seq.java"

mkdir -p "$DIR/androidapp/app/src/main/assets/golib"
xxd -p golib/jni/armeabi-v7a/libgojni.so > "$DIR/androidapp/app/src/main/assets/golib/libgojni-armeabi-v7a.txt"
xxd -p golib/jni/arm64-v8a/libgojni.so > "$DIR/androidapp/app/src/main/assets/golib/libgojni-arm64-v8a.txt"
xxd -p golib/jni/x86/libgojni.so > "$DIR/androidapp/app/src/main/assets/golib/libgojni-x86.txt"
xxd -p golib/jni/x86_64/libgojni.so > "$DIR/androidapp/app/src/main/assets/golib/libgojni-x86_64.txt"
