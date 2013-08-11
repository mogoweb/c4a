#!/bin/bash
#
# Copyright (c) 2013 mogoweb. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.
#

# A script used to sync libraries and source from Chromium project.
# before execute this script, you should place c4a under
# chromium source. We assume this script is in the second level folder of
# chromium's main source directory. you may need modify this script
# according to your source structure.
#
# Use --help to print full usage instructions.
#

PROGNAME=$(basename "$0")
PROGDIR=$(dirname "$0")

# Location of Chromium-top-level sources.
CHROMIUM_SRC=$(cd "$PROGDIR"/.. && pwd 2>/dev/null)

BUILDTYPE=Debug
ANDROID_PROJECT_ROOT=android_project

for opt; do
  optarg=$(expr "x$opt" : 'x[^=]*=\(.*\)')
  case $opt in
    --help|-h|-?)
      HELP=true
      ;;
    --release)
      BUILDTYPE=Release
      ;;
    --debug)
      BUILDTYPE=Debug
      ;;
    -*)
      panic "Unknown option $OPT, see --help." >&2
      ;;
  esac
done

if [ "$HELP" ]; then
  cat <<EOF
Usage: $PROGNAME [options]

Sync libraries and source from Chromium project.

Valid options:
  --help|-h|-?          Print this message.
  --debug               Use libraries under out/Debug.
  --release             Use libraries under out/Release.

EOF

 exit 0
fi

# chromium_mogo
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/chromium_mogo/libs/ ${ANDROID_PROJECT_ROOT}/libs
rsync -avz ${CHROMIUM_SRC}/c4a/browser/java/ ${ANDROID_PROJECT_ROOT}

# Resources.
rsync -avz ${CHROMIUM_SRC}/content/public/android/java/resource_map/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/ui/android/java/resource_map/ ${ANDROID_PROJECT_ROOT}/src/

# ContentView dependencies.
rsync -avz ${CHROMIUM_SRC}/base/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/content/public/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/media/base/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/net/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/ui/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/third_party/eyesfree/src/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/

# Strip a ContentView file that's not supposed to be here.
rm ${ANDROID_PROJECT_ROOT}/src/org/chromium/content/common/common.aidl

# Get rid of the .git directory in eyesfree.
rm -rf ${ANDROID_PROJECT_ROOT}/src/com/googlecode/eyesfree/braille/.git

# Browser components.
rsync -avz ${CHROMIUM_SRC}/components/web_contents_delegate_android/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/components/navigation_interception/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/components/autofill/browser/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/

# Generated files.
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/gen/templates/ ${ANDROID_PROJECT_ROOT}/src/

# Get rid of
rm -rf ${ANDROID_PROJECT_ROOT}/src/org.chromium.content.browser
rm -rf ${ANDROID_PROJECT_ROOT}/src/org.chromium.net

# JARs.
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/lib.java/guava_javalib.jar ${ANDROID_PROJECT_ROOT}/libs/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/lib.java/jsr_305_javalib.jar ${ANDROID_PROJECT_ROOT}/libs/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/lib.java/cacheinvalidation_javalib.jar ${ANDROID_PROJECT_ROOT}/libs/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/lib.java/cacheinvalidation_proto_java.jar ${ANDROID_PROJECT_ROOT}/libs/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/lib.java/protobuf_lite_javalib.jar ${ANDROID_PROJECT_ROOT}/libs/

# chrome & sync
rsync -avz ${CHROMIUM_SRC}/chrome/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/
rsync -avz ${CHROMIUM_SRC}/sync/android/java/src/ ${ANDROID_PROJECT_ROOT}/src/

# chrome res
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/chromium_mogo/gen/org/chromium/chrome/R.java ${ANDROID_PROJECT_ROOT}/src/org/chromium/chrome/R.java
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/chrome.pak ${ANDROID_PROJECT_ROOT}/assets/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/chrome_100_percent.pak ${ANDROID_PROJECT_ROOT}/assets/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/locales/en-US.pak ${ANDROID_PROJECT_ROOT}/assets/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/locales/zh-CN.pak ${ANDROID_PROJECT_ROOT}/assets/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/resources.pak ${ANDROID_PROJECT_ROOT}/assets/
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/gen/webkit/devtools_resources.pak ${ANDROID_PROJECT_ROOT}/assets/

# generated NativeLibraries.java
rsync -avz ${CHROMIUM_SRC}/out/${BUILDTYPE}/chromium_mogo/native_libraries_java/NativeLibraries.java ${ANDROID_PROJECT_ROOT}/src/org/chromium/content/app/NativeLibraries.java
