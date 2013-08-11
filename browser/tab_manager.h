// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef C4A_BROWSER_TAB_MANAGER_H_
#define C4A_BROWSER_TAB_MANAGER_H_

#include <jni.h>

#include "base/android/jni_helper.h"
#include "chrome/browser/ui/android/tab_model/tab_model.h"

class TabManager : public TabModel {
 public:
  explicit TabManager(JNIEnv* env,
                      jobject obj, Profile* profile);

  // Implementation of TabModel
  int GetTabCount() const;
  int GetActiveIndex() const;
  content::WebContents* GetWebContentsAt(int index) const;
  SessionID::id_type GetTabIdAt(int index) const;
  // Used for restoring tabs from synced foreign sessions.
  void CreateTab(content::WebContents* web_contents);
  // Return true if we are currently restoring sessions asynchronously.
  bool IsSessionRestoreInProgress() const;
  void OpenClearBrowsingData() const;

 protected:
  virtual ~TabManager();

 private:
  JavaObjectWeakGlobalRef java_ref_;

  DISALLOW_COPY_AND_ASSIGN(TabManager);
};

// Register the TabManager's native methods through JNI.
bool RegisterTabManager(JNIEnv* env);

#endif // C4A_BROWSER_TAB_MANAGER_H_
