#!/bin/bash
#
# Copyright (c) 2013 mogoweb. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.
#

# A script used to sync java source of android_project to Chromium project.
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
rsync -avz ${ANDROID_PROJECT_ROOT}/src/com/mogoweb/browser/ ${CHROMIUM_SRC}/c4a/browser/java/src/com/mogoweb/browser
rsync -avz ${ANDROID_PROJECT_ROOT}/res/layout/ ${CHROMIUM_SRC}/c4a/browser/java/res/layout/
rsync -avz ${ANDROID_PROJECT_ROOT}/res/xml/ ${CHROMIUM_SRC}/c4a/browser/java/res/xml/
rsync -avz ${ANDROID_PROJECT_ROOT}/assets/ --exclude "*.pak" ${CHROMIUM_SRC}/c4a/browser/java/assets/
rsync -avz ${ANDROID_PROJECT_ROOT}/res/values/strings.xml ${CHROMIUM_SRC}/c4a/browser/java/res/values/strings.xml
rsync -avz ${ANDROID_PROJECT_ROOT}/res/values-zh-rCN/strings.xml ${CHROMIUM_SRC}/c4a/browser/java/res/values-zh-rCN/strings.xml
rsync -avz ${ANDROID_PROJECT_ROOT}/AndroidManifest.xml ${CHROMIUM_SRC}/c4a/browser/java/AndroidManifest.xml

