// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/mogo_google_location_settings_helper.h"

#include "base/android/jni_android.h"
#include "base/android/jni_string.h"
#include "jni/LocationSettingsHelper_jni.h"

using base::android::AttachCurrentThread;
using base::android::ConvertJavaStringToUTF8;

// Factory function
GoogleLocationSettingsHelper* GoogleLocationSettingsHelper::Create() {
  JNIEnv* env = AttachCurrentThread();
  return new MogoGoogleLocationSettingsHelper(env);
}

MogoGoogleLocationSettingsHelper::MogoGoogleLocationSettingsHelper(JNIEnv* env)
    : GoogleLocationSettingsHelper(),
      java_ref_(env, Java_LocationSettingsHelper_getInstance(env, base::android::GetApplicationContext()).obj()) {
}

MogoGoogleLocationSettingsHelper::
    ~MogoGoogleLocationSettingsHelper() {
}

std::string MogoGoogleLocationSettingsHelper::GetAcceptButtonLabel() {
  JNIEnv* env = AttachCurrentThread();
  ScopedJavaLocalRef<jobject> obj = java_ref_.get(env);
  if (obj.is_null())
    return "Allow";

  ScopedJavaLocalRef<jstring> label = Java_LocationSettingsHelper_getAcceptButtonLabel(env, obj.obj());
  if (label.is_null())
    return "Allow";
  else
    return ConvertJavaStringToUTF8(label);
}

void MogoGoogleLocationSettingsHelper::ShowGoogleLocationSettings() {
  JNIEnv* env = AttachCurrentThread();
  ScopedJavaLocalRef<jobject> obj = java_ref_.get(env);
  if (obj.is_null())
    return ;

  Java_LocationSettingsHelper_showGoogleLocationSettings(env, obj.obj());
}

bool MogoGoogleLocationSettingsHelper::
    IsGoogleAppsLocationSettingEnabled() {
  JNIEnv* env = AttachCurrentThread();
  ScopedJavaLocalRef<jobject> obj = java_ref_.get(env);
  if (obj.is_null())
    return false;

  return Java_LocationSettingsHelper_isGoogleAppsLocationSettingEnabled(env, obj.obj());
}

bool MogoGoogleLocationSettingsHelper::IsMasterLocationSettingEnabled() {
  JNIEnv* env = AttachCurrentThread();
  ScopedJavaLocalRef<jobject> obj = java_ref_.get(env);
  if (obj.is_null())
    return false;

  return Java_LocationSettingsHelper_isMasterLocationSettingEnabled(env, obj.obj());
}

bool RegisterLocationSettingsHelper(JNIEnv* env) {
  return RegisterNativesImpl(env);
}
