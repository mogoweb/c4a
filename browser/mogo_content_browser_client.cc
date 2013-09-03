// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/mogo_content_browser_client.h"

#include "c4a/browser/cookie_manager.h"
#include "chrome/browser/profiles/profile.h"
#include "content/public/browser/render_process_host.h"
#include "content/public/browser/render_view_host.h"
#include "net/cookies/cookie_monster.h"
#include "net/url_request/url_request_context.h"
#include "net/url_request/url_request_context_getter.h"
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

void MogoContentBrowserClient::RenderProcessHostCreated(
    content::RenderProcessHost* host) {
  ChromeContentBrowserClient::RenderProcessHostCreated(host);

  int id = host->GetID();
  Profile* profile = Profile::FromBrowserContext(host->GetBrowserContext());
  net::URLRequestContext* context =
      profile->GetRequestContextForRenderProcess(id)->GetURLRequestContext();
  net::CookieMonster* cookie_monster = context->cookie_store()->GetCookieMonster();
  DCHECK(cookie_monster);
  SetCookieMonsterOnNetworkStackInit(cookie_monster);
}
