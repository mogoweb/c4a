// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/mogo_content_browser_client.h"

#include "content/public/browser/render_view_host.h"
#include "webkit/glue/webpreferences.h"

using content::RenderViewHost;

MogoContentBrowserClient::MogoContentBrowserClient()
    : chrome::ChromeContentBrowserClient() {
}

MogoContentBrowserClient::~MogoContentBrowserClient() {
}

void MogoContentBrowserClient::OverrideWebkitPrefs(
    RenderViewHost* rvh, const GURL& url, WebPreferences* web_prefs) {
  chrome::ChromeContentBrowserClient::OverrideWebkitPrefs(rvh, url, web_prefs);
}
