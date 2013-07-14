// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/web_settings.h"

#include "base/android/jni_android.h"
#include "base/android/jni_string.h"
#include "content/public/browser/navigation_controller.h"
#include "content/public/browser/navigation_entry.h"
#include "content/public/browser/render_view_host.h"
#include "content/public/browser/web_contents.h"
#include "content/public/common/content_client.h"
#include "jni/WebSettings_jni.h"
#include "webkit/glue/webpreferences.h"

using base::android::CheckException;
using base::android::ConvertJavaStringToUTF16;
using base::android::ConvertUTF16ToJavaString;
using base::android::ConvertUTF8ToJavaString;
using base::android::GetClass;
using base::android::GetFieldID;
using base::android::GetMethodIDFromClassName;
using base::android::ScopedJavaLocalRef;

struct WebSettings::FieldIds {
  // Note on speed. One may think that an approach that reads field values via
  // JNI is ineffective and should not be used. Please keep in mind that in the
  // legacy WebView the whole Sync method took <1ms on Xoom, and no one is
  // expected to modify settings in performance-critical code.
  FieldIds() { }

  FieldIds(JNIEnv* env) {
    const char* kStringClassName = "Ljava/lang/String;";

    // FIXME: we should be using a new GetFieldIDFromClassName() with caching.
    ScopedJavaLocalRef<jclass> clazz(
        GetClass(env, "com/mogoweb/browser/web/WebSettings"));

    user_agent =
            GetFieldID(env, clazz, "mUserAgent", kStringClassName);
    java_script_enabled =
        GetFieldID(env, clazz, "mJavaScriptEnabled", "Z");
  }

  // Field ids
  jfieldID user_agent;
  jfieldID java_script_enabled;
};

WebSettings::WebSettings(JNIEnv* env, jobject obj)
    : web_settings_(env, obj) {
}

WebSettings::~WebSettings() {
}

void WebSettings::Destroy(JNIEnv* env, jobject obj) {
  delete this;
}

void WebSettings::SetWebContents(JNIEnv* env, jobject obj, jint jweb_contents) {
  content::WebContents* web_contents =
      reinterpret_cast<content::WebContents*>(jweb_contents);
  Observe(web_contents);

  UpdateEverything(env, obj);
}

void WebSettings::UpdateEverything() {
  JNIEnv* env = base::android::AttachCurrentThread();
  CHECK(env);
  ScopedJavaLocalRef<jobject> scoped_obj = web_settings_.get(env);
  jobject obj = scoped_obj.obj();
  if (!obj) return;
  UpdateEverything(env, obj);
}

void WebSettings::UpdateEverything(JNIEnv* env, jobject obj) {
  UpdateWebkitPreferences(env, obj);
}

void WebSettings::UpdateUserAgent(JNIEnv* env, jobject obj) {
  if (!web_contents()) return;

  if (!field_ids_)
    field_ids_.reset(new FieldIds(env));

  ScopedJavaLocalRef<jstring> str(env, static_cast<jstring>(
      env->GetObjectField(obj, field_ids_->user_agent)));
  bool ua_overidden = str.obj() != NULL;

  if (ua_overidden) {
    std::string override = base::android::ConvertJavaStringToUTF8(str);
    web_contents()->SetUserAgentOverride(override);
  }

  const content::NavigationController& controller =
      web_contents()->GetController();
  for (int i = 0; i < controller.GetEntryCount(); ++i)
    controller.GetEntryAtIndex(i)->SetIsOverridingUserAgent(ua_overidden);
}

void WebSettings::UpdateWebkitPreferences(JNIEnv* env, jobject obj) {
  if (!web_contents()) return;

  if (!field_ids_)
    field_ids_.reset(new FieldIds(env));

  content::RenderViewHost* render_view_host =
      web_contents()->GetRenderViewHost();
  if (!render_view_host) return;
  WebPreferences prefs = render_view_host->GetWebkitPreferences();

  prefs.javascript_enabled =
      env->GetBooleanField(obj, field_ids_->java_script_enabled);

  render_view_host->UpdateWebkitPreferences(prefs);
}

void WebSettings::RenderViewCreated(content::RenderViewHost* render_view_host) {
  UpdateEverything();
}

static jint Init(JNIEnv* env,
                 jobject obj,
                 jint web_contents) {
  WebSettings* settings = new WebSettings(env, obj);
  settings->SetWebContents(env, obj, web_contents);
  return reinterpret_cast<jint>(settings);
}

static jstring GetDefaultUserAgent(JNIEnv* env, jclass clazz) {
  return base::android::ConvertUTF8ToJavaString(
      env, content::GetUserAgent(GURL())).Release();
}

bool RegisterWebSettings(JNIEnv* env) {
  return RegisterNativesImpl(env) >= 0;
}
