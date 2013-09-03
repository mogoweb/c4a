// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/chrome_main_delegate_mogo_android.h"

#include "base/android/jni_android.h"
#include "base/android/jni_registrar.h"
#include "base/lazy_instance.h"
#include "c4a/browser/browsing_data_remover.h"
#include "c4a/browser/cookie_manager.h"
#include "c4a/browser/mogo_content_browser_client.h"
#include "c4a/browser/mogo_google_location_settings_helper.h"
#include "c4a/browser/tab_manager.h"
#include "c4a/browser/url_util.h"
#include "c4a/browser/web_settings.h"
#include "c4a/browser/web_tab.h"
#include "chrome/browser/search_engines/template_url_prepopulate_data.h"

static const char kDefaultCountryCode[] = "US";

static base::android::RegistrationMethod kRegistrationMethods[] = {
    { "BrowsingDataRemover", RegisterBrowsingDataRemover },
    { "TabManager", RegisterTabManager },
    { "WebSettings", RegisterWebSettings },
    { "WebTab", WebTab::RegisterWebTab },
    { "LocationSettingsHelper", RegisterLocationSettingsHelper },
    { "CookieManager", RegisterCookieManager },
    { "URLUtil", RegisterURLUtil },
};

base::LazyInstance<MogoContentBrowserClient>
    g_mogo_content_browser_client = LAZY_INSTANCE_INITIALIZER;

ChromeMainDelegateAndroid* ChromeMainDelegateAndroid::Create() {
  return new ChromeMainDelegateMogoAndroid();
}

ChromeMainDelegateMogoAndroid::ChromeMainDelegateMogoAndroid() {
}

ChromeMainDelegateMogoAndroid::~ChromeMainDelegateMogoAndroid() {
}

bool ChromeMainDelegateMogoAndroid::BasicStartupComplete(int* exit_code) {
  TemplateURLPrepopulateData::InitCountryCode(kDefaultCountryCode);
  return ChromeMainDelegateAndroid::BasicStartupComplete(exit_code);
}

bool ChromeMainDelegateMogoAndroid::RegisterApplicationNativeMethods(
    JNIEnv* env) {
  return ChromeMainDelegateAndroid::RegisterApplicationNativeMethods(env) &&
	      base::android::RegisterNativeMethods(env,
	                                           kRegistrationMethods,
	                                           arraysize(kRegistrationMethods));
}

content::ContentBrowserClient*
    ChromeMainDelegateMogoAndroid::CreateContentBrowserClient() {
  return &g_mogo_content_browser_client.Get();
}
