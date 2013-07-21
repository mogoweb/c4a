// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/browsing_data_remover.h"

#include "base/android/jni_android.h"
#include "base/android/jni_helper.h"
#include "chrome/browser/browser_process.h"
#include "chrome/browser/browsing_data/browsing_data_helper.h"
#include "chrome/browser/browsing_data/browsing_data_remover.h"
#include "chrome/browser/profiles/profile.h"
#include "chrome/browser/profiles/profile_manager.h"
#include "jni/BrowsingDataRemover_jni.h"

class ClearDataObserver : public BrowsingDataRemover::Observer {
 public:
  ClearDataObserver(JNIEnv* env, jobject obj) : java_ref_(env, obj) { }

  void OnBrowsingDataRemoverDone() {
    JNIEnv* env = base::android::AttachCurrentThread();
    ScopedJavaLocalRef<jobject> obj = java_ref_.get(env);
    if (obj.is_null())
      return;

    Java_BrowsingDataRemover_onBrowsingDataRemoverDone(env, obj.obj());

    delete this;
  }

 private:
  JavaObjectWeakGlobalRef java_ref_;
};

bool RegisterBrowsingDataRemover(JNIEnv* env) {
  return RegisterNativesImpl(env);
}

static void ClearData(JNIEnv* env, jobject obj, jint mask) {
  ClearDataObserver* observer = new ClearDataObserver(env, obj);

  Profile* profile = g_browser_process->profile_manager()->GetLastUsedProfile();
  BrowsingDataRemover* remover =
      BrowsingDataRemover::CreateForUnboundedRange(profile);
  remover->AddObserver(observer);
  remover->Remove(mask, BrowsingDataHelper::UNPROTECTED_WEB);

  // BrowsingDataRemover takes care of deleting itself when done.
}



