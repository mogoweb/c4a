// Copyright (c) 2013 mogoweb. All rights reserved.
// Copyright (c) 2011 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef C4A_BROWSER_COOKIE_MANAGER_H_
#define C4A_BROWSER_COOKIE_MANAGER_H_

#include <jni.h>

namespace net {
class CookieMonster;
}  // namespace net

class AwURLRequestJobFactory;

void SetCookieMonsterOnNetworkStackInit(net::CookieMonster* cookie_monster);

bool RegisterCookieManager(JNIEnv* env);

#endif  // ANDROID_WEBVIEW_NATIVE_COOKIE_MANAGER_H_
