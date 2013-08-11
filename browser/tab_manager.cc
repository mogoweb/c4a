// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/tab_manager.h"

#include "chrome/browser/browser_process.h"
#include "chrome/browser/profiles/profile.h"
#include "chrome/browser/profiles/profile_manager.h"
#include "chrome/browser/ui/android/tab_model/tab_model_list.h"
#include "jni/TabManager_jni.h"

using base::android::AttachCurrentThread;

TabManager::TabManager(JNIEnv* env,
                         jobject obj, Profile* profile)
    : TabModel(profile)
    , java_ref_(env, obj) {
  TabModelList::AddTabModel(this);
}

int TabManager::GetTabCount() const {
  JNIEnv* env = AttachCurrentThread();
  ScopedJavaLocalRef<jobject> obj = java_ref_.get(env);
  if (obj.is_null())
    return 0;

  return Java_TabManager_getTabsCount(env, obj.obj());
}

int TabManager::GetActiveIndex() const {
  JNIEnv* env = AttachCurrentThread();
  ScopedJavaLocalRef<jobject> obj = java_ref_.get(env);
  if (obj.is_null())
    return -1;

  return Java_TabManager_getActiveTabIndex(env, obj.obj());
}

content::WebContents* TabManager::GetWebContentsAt(int index) const {
  return NULL;
}

SessionID::id_type TabManager::GetTabIdAt(int index) const {
  NOTIMPLEMENTED();
  return 0;
}

// Used for restoring tabs from synced foreign sessions.
void TabManager::CreateTab(content::WebContents* web_contents) {
  NOTIMPLEMENTED();
}

// Return true if we are currently restoring sessions asynchronously.
bool TabManager::IsSessionRestoreInProgress() const {
  NOTIMPLEMENTED();
  return false;
}

void TabManager::OpenClearBrowsingData() const {
  JNIEnv* env = AttachCurrentThread();
  ScopedJavaLocalRef<jobject> obj = java_ref_.get(env);
  if (obj.is_null())
    return;

  Java_TabManager_openClearBrowsingData(env, obj.obj());
}

TabManager::~TabManager() {
}

bool RegisterTabManager(JNIEnv* env) {
  return RegisterNativesImpl(env);
}

static jint Init(JNIEnv* env,
                 jobject obj) {
  Profile* profile = g_browser_process->profile_manager()->GetLastUsedProfile();
  TabManager* tabManager = new TabManager(
      env,
      obj,
      profile);
  return reinterpret_cast<jint>(tabManager);
}
