// Copyright (c) 2013 mogoweb. All rights reserved.
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

import org.chromium.content.common.CommandLine;

import android.app.ActivityManager;
import android.content.Context;

import com.mogoweb.browser.Tab.Embodiment;
import com.mogoweb.browser.TabManager.TabData;
import com.mogoweb.browser.preferences.BrowserPreferences;
import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.web.WebTab;

public class MemoryMonitor implements TabManager.Listener, Tab.Listener {

    private final Context mContext;
    //This number is used with device memory class to calculate max number
    //of active tabs.
    private static final int MEMORY_PER_TAB = 16;
    private int MAX_ACTIVE_TABS = 0;
    private TabManager mTabManager;
    private static MemoryMonitor sMemoryMonitor;
    private static boolean mListenerActive = false;

    // Should be called only once
    public static MemoryMonitor create(Context context) {

        if(sMemoryMonitor == null) {
            sMemoryMonitor = new MemoryMonitor(context);
        }
        return sMemoryMonitor;
    }

    public static MemoryMonitor getInstance() {
        return sMemoryMonitor;
    }

    MemoryMonitor(Context context) {
        mContext = context;
        MAX_ACTIVE_TABS = getMaxActiveTabs(mContext);
        Logger.debug("MemoryMonitor Max Tabs "+ MAX_ACTIVE_TABS);
        mTabManager = TabManager.getInstance();
        updateListener();
    }

    private void addListener() {
        mListenerActive = true;
        mTabManager.addListener(this);
    }

    private void removeListener() {
        mTabManager.removeListener(sMemoryMonitor);
        mListenerActive = false;
    }

    public void updateListener() {
        if(!CommandLine.getInstance().hasSwitch(CommandLine.DISABLE_MEMORY_MONITOR)) {

            if(BrowserPreferences.getInstance().getMemoryMonitorEnabled())
                addListener();
            else
                removeListener();
        }
    }

    public int getActiveTabs() {
        int numNativeActiveTab = 0;
        for(int i=0; i < mTabManager.getTabsCount(); i++) {
            Tab tab = mTabManager.getTab(i);
            if(isWebTab(tab)) {
                if (((WebTab)tab).isNativeActive())
                    numNativeActiveTab++;
            }
        }
        return numNativeActiveTab;
    }

    public boolean isWebTab(Tab tab) {
        if (tab.getEmbodiment() == Embodiment.E_Web)
            return true;
        return false;
    }

    //if number of tabs whose native tab is active, is greater
    //than MAX_ACTIVE_TABS destroy the nativetab of oldest used Tab
    public void destroyNativeTab() {
        int numTabs = getActiveTabs();
        int totalTabs = mTabManager.getTabsCount();
        Logger.debug("MemoryMonitor Max Tabs "+ MAX_ACTIVE_TABS
                     + "numTabs "+numTabs);
        if(numTabs > MAX_ACTIVE_TABS) {
            TabData lastUsedTabData = null;
            TabData temp = null;

            for (int i = 0; i < totalTabs; i++) {
                TabData td = mTabManager.getTabData(i);
                if(isWebTab(td.tab)) {
                    if(((WebTab)td.tab).isNativeActive()) {
                        if(lastUsedTabData == null)
                            lastUsedTabData = td;
                        else {
                            temp = td;
                            if(temp.timestamp.compareTo(lastUsedTabData.timestamp) < 0)
                                lastUsedTabData = temp;
                        }
                    }
                }
            }

            ((WebTab)lastUsedTabData.tab).killContentLayer();
        }
    }

    @Override
    public void onTabAdded(TabData td, int idx) {
        Logger.debug("MemoryMonitor onTabAdded");
    }

    @Override
    public void onTabRemoved(TabData td, int idx) {
        Logger.debug("MemoryMonitor onTabRemoved");
    }

    @Override
    public void onTabSelected(TabData td, boolean bActive) {
        Logger.debug("MemoryMonitor onTabSelected bActive:"+bActive);
        if(bActive)
            td.tab.addListener(this);
        else
            td.tab.removeListener(this);
    }

    @Override
    public void onTabShow(TabData tab) {
        Logger.debug("MemoryMonitor onTabShow");
    }

    /**
    * Returns the default max number of active tabs based on device's
    * memory class.
    */
    static int getMaxActiveTabs(Context context) {
//        // We use device memory class to decide number of active tabs
//        // (minimum memory class is 16).
//        ActivityManager am =
//          (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        if(am.getMemoryClass() < 33)
//          return 1;
//        else
//          return 2;
        return 1;
    }

    // Tab.Listener implementation
    @Override
    public void onLoadProgressChanged(int progress) {}

    @Override
    public void onUpdateUrl(String url) {}

    @Override
    public void onLoadStarted(boolean isMainFrame) {
        if(isMainFrame) {
             Logger.debug("MemoryMonitor didStartLoading");
             destroyNativeTab();
        }
    }

    @Override
    public void onLoadStopped(boolean isMainFrame) {}

    @Override
    public void didFailLoad(boolean isProvisionalLoad, boolean isMainFrame, int errorCode,
            String description, String failingUrl) {}

    @Override
    public void showContextMenu(String url) {}

}
