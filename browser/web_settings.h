// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef C4A_BROWSER_WEB_SETTINGS_H_
#define C4A_BROWSER_WEB_SETTINGS_H_

#include <jni.h>

#include "base/android/jni_helper.h"
#include "base/android/scoped_java_ref.h"
#include "base/memory/scoped_ptr.h"
#include "content/public/browser/web_contents_observer.h"

class WebSettings : public content::WebContentsObserver {
 public:
  WebSettings(JNIEnv* env, jobject obj);
  virtual ~WebSettings();

  // Called from Java.
  void Destroy(JNIEnv* env, jobject obj);
  void SetWebContents(JNIEnv* env, jobject obj, jint web_contents);
  void UpdateEverything(JNIEnv* env, jobject obj);
  void UpdateWebkitPreferences(JNIEnv* env, jobject obj);
 private:
  struct FieldIds;

  void UpdateEverything();

  // WebContentsObserver overrides:
  virtual void RenderViewCreated(
      content::RenderViewHost* render_view_host) OVERRIDE;

  // Java field references for accessing the values in the Java object.
  scoped_ptr<FieldIds> field_ids_;

  JavaObjectWeakGlobalRef web_settings_;
};

bool RegisterWebSettings(JNIEnv* env);

#endif // C4A_BROWSER_WEB_SETTINGS_H_
