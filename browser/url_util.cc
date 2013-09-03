// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/url_util.h"

#include "base/android/jni_string.h"
#include "base/string_util.h"
#include "jni/URLUtil_jni.h"
#include "net/base/net_util.h"
#include "url/gurl.h"

using base::android::ConvertJavaStringToUTF8;
using base::android::ConvertJavaStringToUTF16;

jstring GetSuggestedFilename(JNIEnv* env, jclass javaClass,
            jstring jurl,
            jstring jcontentDisposition,
            jstring jmimeType) {
  GURL url(ConvertJavaStringToUTF16(env, jurl));
  std::string content_disposition = ConvertJavaStringToUTF8(env, jcontentDisposition);
  std::string mime_type = ConvertJavaStringToUTF8(env, jmimeType);

  return base::android::ConvertUTF16ToJavaString(
      env,
      net::GetSuggestedFilename(url, content_disposition, EmptyString(), EmptyString(), mime_type, "download")).Release();
}

bool RegisterURLUtil(JNIEnv* env) {
  return RegisterNativesImpl(env);
}
