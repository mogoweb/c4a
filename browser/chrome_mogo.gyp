# Copyright (c) 2012 The Chromium Authors. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.
{
  'variables': {
    'chromium_code': 1,
    'package_name': 'chromium_mogo',
  },
  'includes': [
    '../../chrome/chrome_android_paks.gypi', # Included for the list of pak resources.
  ],
  'targets': [
    {
      'target_name': 'libchromium_mogo',
      'type': 'shared_library',
      'dependencies': [
        '../../base/base.gyp:base',
        '../../chrome/chrome.gyp:chrome_android_core',
        '../../chrome/chrome.gyp:browser_ui',
        'chromium_mogo_auxiliary',
        'chromium_mogo_jni_headers',
      ],
      'sources': [
        # This file must always be included in the shared_library step to ensure
        # JNI_OnLoad is exported.
        '<(DEPTH)/chrome/app/android/chrome_jni_onload.cc',
        '<(DEPTH)/c4a/browser/browsing_data_remover.cc',
        '<(DEPTH)/c4a/browser/browsing_data_remover.h',
        '<(DEPTH)/c4a/browser/chrome_main_delegate_mogo_android.cc',
        '<(DEPTH)/c4a/browser/chrome_main_delegate_mogo_android.h',
        '<(DEPTH)/c4a/browser/mogo_google_location_settings_helper.cc',
        '<(DEPTH)/c4a/browser/mogo_google_location_settings_helper.h',
        '<(DEPTH)/c4a/browser/state_serializer.cc',
        '<(DEPTH)/c4a/browser/state_serializer.h',
        '<(DEPTH)/c4a/browser/web_settings.cc',
        '<(DEPTH)/c4a/browser/web_settings.h',
        '<(DEPTH)/c4a/browser/web_tab.cc',
        '<(DEPTH)/c4a/browser/web_tab.h',
      ],
      'include_dirs': [
        '<(SHARED_INTERMEDIATE_DIR)/chromium_mogo',
        '../../skia/config',
      ],
      'conditions': [
        [ 'order_profiling!=0', {
          'conditions': [
            [ 'OS=="android"', {
              'dependencies': [ '../../tools/cygprofile/cygprofile.gyp:cygprofile', ],
            }],
          ],
        }],
      ],
    },
    {
      'target_name': 'chromium_mogo',
      'type': 'none',
      'dependencies': [
        '../../media/media.gyp:media_java',
        '../../chrome/chrome.gyp:chrome_java',
        '../../third_party/android_tools/android_tools.gyp:android_support_v4',
        'chromium_mogo_paks',
        'libchromium_mogo',
      ],
      'variables': {
        'apk_name': 'ChromiumMogo',
        'manifest_package_name': 'com.mogoweb.browser',
        'java_in_dir': '<(DEPTH)/c4a/browser/java',
        'resource_dir': 'java/res',
        'asset_location': '<(ant_build_out)/../assets/<(package_name)',
        'native_lib_target': 'libchromium_mogo',
        'additional_input_paths': [
          '<@(chrome_android_pak_output_resources)',
          '<(chrome_android_pak_output_folder)/devtools_resources.pak',
        ],
      },
      'includes': [ '../../build/java_apk.gypi', ],
    },
    {
      'target_name': 'chromium_mogo_jni_headers',
      'type': 'none',
      'sources': [
        'java/src/com/mogoweb/browser/BrowsingDataRemover.java',
        'java/src/com/mogoweb/browser/web/WebSettings.java',
        'java/src/com/mogoweb/browser/web/WebTab.java',
      ],
      'variables': {
        'jni_gen_package': 'chromium_mogo',
      },
      'includes': [ '../../build/jni_generator.gypi' ],
    },
    {
      # chromium_mogo creates a .jar as a side effect. Any java targets
      # that need that .jar in their classpath should depend on this target,
      # chromium_mogo_java. Dependents of chromium_mogo receive its
      # jar path in the variable 'apk_output_jar_path'.
      'target_name': 'chromium_mogo_java',
      'type': 'none',
      'dependencies': [
        'chromium_mogo',
      ],
      'includes': [ '../../build/apk_fake_jar.gypi' ],
    },
    {
       'target_name': 'chromium_mogo_auxiliary',
       'type': 'static_library',
       'include_dirs': [
         '<(SHARED_INTERMEDIATE_DIR)/chromium_mogo',
       ],
       'sources': [
         'chrome_data_reduction_proxy_mogo_android.cc',
       ],
       'dependencies': [
         '../../base/base.gyp:base',
       ],
    },
    {
      'target_name': 'chromium_mogo_paks',
      'type': 'none',
      'dependencies': [
        '<(DEPTH)/chrome/chrome_resources.gyp:packed_resources',
        '<(DEPTH)/chrome/chrome_resources.gyp:packed_extra_resources',
      ],
      'copies': [
        {
          'destination': '<(chrome_android_pak_output_folder)',
          'files': [
            '<@(chrome_android_pak_input_resources)',
            '<(SHARED_INTERMEDIATE_DIR)/webkit/devtools_resources.pak',
          ],
        }
      ],
    },
  ],
}
