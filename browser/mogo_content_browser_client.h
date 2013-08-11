// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef C4A_MOGO_CONTENT_BROWSER_CLIENT_H_
#define C4a_MOGO_CONTENT_BROWSER_CLIENT_H_

#include "chrome/browser/chrome_content_browser_client.h"

class MogoContentBrowserClient : public chrome::ChromeContentBrowserClient {
 public:
  MogoContentBrowserClient();
  virtual ~MogoContentBrowserClient();

  virtual void OverrideWebkitPrefs(content::RenderViewHost* rvh,
                                   const GURL& url,
                                   WebPreferences* prefs) OVERRIDE;
};

#endif // C4a_MOGO_CONTENT_BROWSER_CLIENT_H_
