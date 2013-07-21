// Copyright (c) 2013 mogoweb. All rights reserved.
// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef C4A_BROWSER_WEB_TAB_H_
#define C4A_BROWSER_WEB_TAB_H_

#include <jni.h>

#include "base/compiler_specific.h"
#include "base/memory/scoped_ptr.h"
#include "chrome/browser/android/tab_android.h"

namespace browser_sync {
class SyncedTabDelegate;
}

namespace chrome {
namespace android {
class ChromeWebContentsDelegateAndroid;
}
}

namespace content {
class WebContents;
}

namespace ui {
class WindowAndroid;
}

class WebTab : public TabAndroid {
 public:
  WebTab(JNIEnv* env,
               jobject obj,
               content::WebContents* web_contents,
               ui::WindowAndroid* window_android);
  void Destroy(JNIEnv* env, jobject obj);

  // --------------------------------------------------------------------------
  // TabAndroid Methods
  // --------------------------------------------------------------------------
  virtual content::WebContents* GetWebContents() OVERRIDE;

  virtual browser_sync::SyncedTabDelegate* GetSyncedTabDelegate() OVERRIDE;

  virtual void OnReceivedHttpAuthRequest(jobject auth_handler,
                                         const string16& host,
                                         const string16& realm) OVERRIDE;
  virtual void ShowContextMenu(
      const content::ContextMenuParams& params) OVERRIDE;

  virtual void ShowCustomContextMenu(
      const content::ContextMenuParams& params,
      const base::Callback<void(int)>& callback) OVERRIDE;

  virtual void AddShortcutToBookmark(const GURL& url,
                                     const string16& title,
                                     const SkBitmap& skbitmap,
                                     int r_value,
                                     int g_value,
                                     int b_value) OVERRIDE;
  virtual void EditBookmark(int64 node_id, bool is_folder) OVERRIDE;

  virtual void RunExternalProtocolDialog(const GURL& url) OVERRIDE;

  base::android::ScopedJavaLocalRef<jbyteArray> GetOpaqueState(
      JNIEnv* env, jobject obj);
  jboolean RestoreState(JNIEnv* env, jobject obj, jbyteArray state);

  // Register the Tab's native methods through JNI.
  static bool RegisterWebTab(JNIEnv* env);

  // --------------------------------------------------------------------------
  // Methods called from Java via JNI
  // --------------------------------------------------------------------------
  void InitWebContentsDelegate(JNIEnv* env,
                               jobject obj,
                               jobject web_contents_delegate);

  base::android::ScopedJavaLocalRef<jstring> FixupUrl(JNIEnv* env,
                                                      jobject obj,
                                                      jstring url);

 protected:
  virtual ~WebTab();

 private:
  JavaObjectWeakGlobalRef java_ref_;
  scoped_ptr<content::WebContents> web_contents_;
  scoped_ptr<chrome::android::ChromeWebContentsDelegateAndroid>
          web_contents_delegate_;

  DISALLOW_COPY_AND_ASSIGN(WebTab);
};

#endif  // C4A_BROWSER_TAB_H_
