/*
 *  Copyright (c) 2012-2013, The Linux Foundation. All rights reserved.
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

package com.mogoweb.browser;

import android.graphics.Bitmap;

import org.chromium.content.browser.ContentView;
/**
 * While this is just an initial 'isolation' interface, it shows what we need
 * from the tab from an outside perspective. No implementation-specific types
 * should leak from this interface.
 *
 * This abstraction is in place (and used in TabManager.TabData.tabImpl) just
 * so that we'll be able to switch from a 'Web' tab to a 'Reader' or any kind
 * of tab.
 */
public interface Tab {

    public enum Embodiment {

        // the tab contains the welcome image
        E_Welcome,

        // the default tab, showing a web page
        E_Web
    }

    public interface Listener {
        void onLoadProgressChanged(int progress);
        void onLoadStarted(boolean isMainFrame); // Frame loads
        void onLoadStopped(boolean isMainFrame); // Frame loads
        void onUpdateUrl(String url);
        void didFailLoad(boolean isProvisionalLoad, boolean isMainFrame, int errorCode,
            String description, String failingUrl);
        void showContextMenu(String url);
        void didStartLoading(String url); // Page Load
        void didStopLoading(String url); // Page Load
    }

    void addListener(Listener li);
    void removeListener(Listener li);

    Embodiment getEmbodiment();
    void loadUrl(String string);
    int getCurrentLoadProgress();
    void stopLoading();
    void reload();
    String getUrl();
    // commented since it's not used and we keep this minimal
    // String getOriginalUrl();
    // TODO: clarify the difference of setUrl vs loadUrl...
    void setUrl(String url);
    String getTitle();
    Bitmap getSnapshot(int width, int height);
    boolean getUseDesktopUserAgent();
    void setUseDesktopUserAgent(boolean on);
    void destroy();
    void goBack();
    void goForward();
    boolean canGoForward();
    boolean canGoBack();

    /**
     * @return Tab's serialized state or null if not a WebTab.
     */
    byte[] getState();

    // Added for testing
    String getSnapshotFilename();

    void onActivityPause();
    void onActivityResume();
    void onShow();
    void onHide();

    ContentView getContentView();
}
