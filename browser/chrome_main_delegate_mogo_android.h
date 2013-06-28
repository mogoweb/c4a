// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef C4A_CHROME_MAIN_DELEGATE_MOGO_ANDROID_H_
#define C4A_CHROME_MAIN_DELEGATE_MOGO_ANDROID_H_

#include "chrome/app/android/chrome_main_delegate_android.h"

class ChromeMainDelegateMogoAndroid : public ChromeMainDelegateAndroid {
 public:
  ChromeMainDelegateMogoAndroid();
  virtual ~ChromeMainDelegateMogoAndroid();

  virtual bool BasicStartupComplete(int* exit_code) OVERRIDE;

  virtual bool RegisterApplicationNativeMethods(JNIEnv* env) OVERRIDE;

 private:
  DISALLOW_COPY_AND_ASSIGN(ChromeMainDelegateMogoAndroid);
};

#endif  // C4A_MOGO_CHROME_MAIN_DELEGATE_MOGO_ANDROID_H_
