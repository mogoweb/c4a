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

import org.chromium.base.AccessedByNative;
import org.chromium.base.CalledByNative;
import org.chromium.base.JNINamespace;

import com.mogoweb.browser.Tab;
import com.mogoweb.browser.Tab.Embodiment;
import com.mogoweb.browser.TabManager;
import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.web.WebTab;

@JNINamespace("content")
public class BrowserPreferences {
    private final Context mContext;
    private static BrowserPreferences browserPrefs;
    private final TabManager mTabManager;

    @AccessedByNative
    private static boolean mJavaScriptEnabled = true;
    @AccessedByNative
    private static boolean mAllowPopupsEnabled = false;

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
    }

    public boolean getJavaScriptEnabled() {
        return mJavaScriptEnabled;
    }

    public boolean getPopupsEnabled() {
        return mAllowPopupsEnabled;
    }

    //overload this method if/when we have a preference value other than boolean
    public void setPreference(String key, boolean enabled) {
        WebTab webTab;
        Tab tab;

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

    @CalledByNative
    private static Boolean getPreferenceBoolean(String title) {
        return true;
    }
}
