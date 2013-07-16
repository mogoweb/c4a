/*
 *  Copyright (c) 2013, The Linux Foundation. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *      * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 *  ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 *  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 *  IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.mogoweb.browser.preferences;

import android.content.Context;
import android.preference.PreferenceManager;

import com.mogoweb.browser.MemoryMonitor;
import com.mogoweb.browser.Tab;
import com.mogoweb.browser.Tab.Embodiment;
import com.mogoweb.browser.TabManager;
import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.web.WebSettings;
import com.mogoweb.browser.web.WebTab;

public class BrowserPreferences {
    private final Context mContext;
    private static BrowserPreferences browserPrefs;
    private final TabManager mTabManager;

    private static boolean mJavaScriptEnabled = true;
    private static boolean mAllowPopupsEnabled = false;
    private static String mUserAgent = "";
    private static boolean mMemoryMonitorEnabled = true;

    public static BrowserPreferences create(Context context) {
        if (browserPrefs == null) {
            browserPrefs = new BrowserPreferences(context);
        }
        return browserPrefs;
    }

    public static BrowserPreferences getInstance() {
        return browserPrefs;
    }

    private BrowserPreferences(Context context) {
        mContext = context;
        mTabManager = TabManager.getInstance();

        // ensure singleton
        if (browserPrefs != null) {
            Logger.error("BrowserPreferences already created");
            return;
        }

        browserPrefs = this;

        //set preferences based on user settings menu
        mJavaScriptEnabled = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PreferenceKeys.PREF_ENABLE_JAVASCRIPT, true);
        mAllowPopupsEnabled = !(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PreferenceKeys.PREF_BLOCK_POPUPS, true));
        mUserAgent = PreferenceManager.getDefaultSharedPreferences(mContext).getString(PreferenceKeys.PREF_USER_AGENT, "");
        mMemoryMonitorEnabled = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(PreferenceKeys.PREF_ENABLE_MEMORY_MONITOR, true);
    }

    public boolean getJavaScriptEnabled() {
        return mJavaScriptEnabled;
    }

    public boolean getPopupsEnabled() {
        return mAllowPopupsEnabled;
    }

    public String getUserAgentString() {
        if (mUserAgent.equals("0")) {
            return WebSettings.getDefaultUserAgent();
        } else if (mUserAgent.equals("1")) {
            return "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.63 Safari/537.31";
        } else if (mUserAgent.equals("2")) {
            return "Mozilla/5.0 (iPhone; CPU iPhone OS 6_1_4 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10B350 Safari/8536.25";
        }

        return WebSettings.getDefaultUserAgent();
    }

    public boolean getMemoryMonitorEnabled() {
        return mMemoryMonitorEnabled;
    }

    public void setPreference(String key, boolean enabled) {
        WebTab webTab;
        Tab tab;

        // No need to call this per tab bases.
        if (key.equals(PreferenceKeys.PREF_ENABLE_MEMORY_MONITOR)) {
            mMemoryMonitorEnabled = enabled;
            MemoryMonitor.getInstance().updateListener();
        } else {
            for (int i = 0; i < mTabManager.getTabsCount(); i++) {
                tab = mTabManager.getTabData(i).tab;
                if (tab.getEmbodiment() == Embodiment.E_Web) {
                    webTab = (WebTab)tab;

                    if (key.equals(PreferenceKeys.PREF_ENABLE_JAVASCRIPT)){
                        mJavaScriptEnabled = enabled;
                        webTab.getSettings().setJavaScriptEnabled(enabled);
                    }
                    else if (key.equals(PreferenceKeys.PREF_BLOCK_POPUPS)) {
                        mAllowPopupsEnabled = enabled;
                        webTab.getSettings().setJavaScriptCanOpenWindowsAutomatically(enabled);
                    }
                }
            }
        }
    }

    public void setPreference(String key, String value) {
        WebTab webTab;
        Tab tab;

        for (int i = 0; i < mTabManager.getTabsCount(); i++) {
            tab = mTabManager.getTabData(i).tab;
            if (tab.getEmbodiment() == Embodiment.E_Web) {
                webTab = (WebTab)tab;

                if (key.equals(PreferenceKeys.PREF_USER_AGENT)
                        && !mUserAgent.equals(value)) {
                    mUserAgent = value;
                    webTab.getSettings().setUserAgentString(getUserAgentString());
                }
            }
       }
    }
}
