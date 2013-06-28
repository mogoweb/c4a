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

package com.mogoweb.browser;

import android.graphics.Bitmap;

import org.chromium.content.browser.ContentView;

public class DummyTab implements Tab {
    private Tab mTab;
    private boolean bDesktop;
    private final Embodiment mEmbodiment;

    public DummyTab() {
        mEmbodiment = Tab.Embodiment.E_Welcome;
    }

    public DummyTab(Embodiment e, Tab tab) {
        mEmbodiment = e;
        mTab = tab;
    }

    @Override
    public void loadUrl(String string) {
        if (mTab != null)
            mTab.loadUrl(string);
    }

    @Override
    public int getCurrentLoadProgress() {
        return 0;
    }

    @Override
    public void stopLoading() {
    }

    @Override
    public void reload() {
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public void setUrl(String url) {
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Bitmap getSnapshot(int width, int height) {
        return null;
    }

    @Override
    public boolean getUseDesktopUserAgent() {
        return bDesktop;
    }

    @Override
    public void setUseDesktopUserAgent(boolean on) {
        bDesktop = on;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void goBack() {
    }

    @Override
    public void goForward() {
    }

    @Override
    public boolean canGoForward() {
        return false;
    }

    @Override
    public boolean canGoBack() {
        return false;
    }

    @Override
    public byte[] getState() {
        return null;
    }

    @Override
    public Embodiment getEmbodiment() {
        return mEmbodiment;
    }

    @Override
    public void onActivityPause() {}

    @Override
    public void onActivityResume() {}

    @Override
    public void onShow() {}

    @Override
    public void onHide() {}

    @Override
    public void addListener(Tab.Listener li) {}

    @Override
    public void removeListener(Tab.Listener li) {}

    @Override
    public String getSnapshotFilename() {
        return null;
    }

    @Override
    public ContentView getContentView() {
        return null;
    }
}
