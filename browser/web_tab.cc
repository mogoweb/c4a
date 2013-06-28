// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/web_tab.h"

#include "base/android/jni_string.h"
#include "base/logging.h"
#include "chrome/browser/android/chrome_web_contents_delegate_android.h"
#include "chrome/browser/net/url_fixer_upper.h"
#include "chrome/browser/ui/android/window_android_helper.h"
#include "content/public/browser/android/content_view_core.h"
#include "content/public/browser/web_contents.h"
#include "googleurl/src/gurl.h"
#include "jni/WebTab_jni.h"
#include "ui/android/window_android.h"

using base::android::ConvertJavaStringToUTF8;
using base::android::ConvertUTF8ToJavaString;
using base::android::ScopedJavaLocalRef;
using chrome::android::ChromeWebContentsDelegateAndroid;
using content::WebContents;
using ui::WindowAndroid;

WebTab::WebTab(JNIEnv* env,
                           jobject obj,
                           WebContents* web_contents,
                           WindowAndroid* window_android)
    : TabAndroid(env, obj),
      web_contents_(web_contents) {
  InitTabHelpers(web_contents);
  WindowAndroidHelper::FromWebContents(web_contents)->
      SetWindowAndroid(window_android);
}

WebTab::~WebTab() {
}

void WebTab::Destroy(JNIEnv* env, jobject obj) {
  delete this;
}

WebContents* WebTab::GetWebContents() {
  return web_contents_.get();
}

browser_sync::SyncedTabDelegate* WebTab::GetSyncedTabDelegate() {
  NOTIMPLEMENTED();
  return NULL;
}

void WebTab::OnReceivedHttpAuthRequest(jobject auth_handler,
                                             const string16& host,
                                             const string16& realm) {
  NOTIMPLEMENTED();
}

void WebTab::ShowContextMenu(
    const content::ContextMenuParams& params) {
  NOTIMPLEMENTED();
}

void WebTab::ShowCustomContextMenu(
    const content::ContextMenuParams& params,
    const base::Callback<void(int)>& callback) {
  NOTIMPLEMENTED();
}

void WebTab::AddShortcutToBookmark(
    const GURL& url, const string16& title, const SkBitmap& skbitmap,
    int r_value, int g_value, int b_value) {
  NOTIMPLEMENTED();
}

void WebTab::EditBookmark(int64 node_id, bool is_folder) {
  NOTIMPLEMENTED();
}

void WebTab::RunExternalProtocolDialog(const GURL& url) {
  NOTIMPLEMENTED();
}

bool WebTab::RegisterWebTab(JNIEnv* env) {
  return RegisterNativesImpl(env);
}

void WebTab::InitWebContentsDelegate(
    JNIEnv* env,
    jobject obj,
    jobject web_contents_delegate) {
  web_contents_delegate_.reset(
      new ChromeWebContentsDelegateAndroid(env, web_contents_delegate));
  web_contents_->SetDelegate(web_contents_delegate_.get());
}

ScopedJavaLocalRef<jstring> WebTab::FixupUrl(JNIEnv* env,
                                                   jobject obj,
                                                   jstring url) {
  GURL fixed_url(URLFixerUpper::FixupURL(ConvertJavaStringToUTF8(env, url),
                                         std::string()));

  std::string fixed_spec;
  if (fixed_url.is_valid())
    fixed_spec = fixed_url.spec();

  return ConvertUTF8ToJavaString(env, fixed_spec);
}

static jint Init(JNIEnv* env,
                 jobject obj,
                 jint web_contents_ptr,
                 jint window_android_ptr) {
  WebTab* tab = new WebTab(
      env,
      obj,
      reinterpret_cast<WebContents*>(web_contents_ptr),
      reinterpret_cast<WindowAndroid*>(window_android_ptr));
  return reinterpret_cast<jint>(tab);
}
