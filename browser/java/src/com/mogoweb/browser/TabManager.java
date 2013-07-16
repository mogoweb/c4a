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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.chromium.base.CalledByNative;
import org.chromium.base.JNINamespace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Base64;

import com.mogoweb.browser.Intention.Type;
import com.mogoweb.browser.Tab.Embodiment;
import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.web.WebTab;

public class TabManager implements WebTab.ClientDelegate {
    public static final String DEFAULT_SEARCH_QUERY_PREFIX = "http://www.google.com/search?q=";

    private static TabManager sTabManager;
    private final Activity mActivity;
    private final Context mContext;
    private final List<TabData> mTabs;
    private TabData mActiveTab;
    private Tab mDummyTab;
    private final List<Listener> mListeners = new ArrayList<Listener>();

    // Parameters used for saving and restoring Tab state
    private static final String TAB_STATE_FILENAME = "tab_state.json";
    private static final String TAB_SNAPSHOT_FILENAME_PREFIX = "tab_snapshot_";
    private static final String NUM_TABS_KEY = "NumTabs";
    private static final String ACTIVE_TAB_KEY = "ActiveTabIndex";
    private static final String TAB_ARRAY_KEY = "TabArray";
    private static final String TAB_EMBODIMENT_KEY = "TabEmbodiment";
    private static final String TAB_STATE_KEY = "TabState";
    private static final String TAB_TITLE_KEY = "TabTitle";
    private static final String TAB_SNAPSHOT_FILE_KEY = "TabSnapshotFile";
    private static final String TAB_PARENT_KEY = "TabParent";

    public class TabData {
        public TabData parent;
        public Tab tab;
        public Timestamp timestamp;
    }

    public interface Listener {

        void onTabAdded(TabData tab, int location);
        void onTabRemoved(TabData tab, int location);
        void onTabSelected(TabData tab, boolean bSelected);
        void onTabShow(TabData tab);
    }

    // Should be called only once
    public static TabManager create(Activity activity) {
        if (sTabManager == null) {
            sTabManager = new TabManager(activity);
        }
        return sTabManager;
    }

    public static TabManager getInstance() {
        return sTabManager;
    }

    private TabManager(Activity activity) {
        mActivity = activity;
        mContext = activity;
        mTabs = new ArrayList<TabData>();

        // ensure tab manager is singleton
        if (sTabManager != null) {
            Logger.error("TabManager already created");
            return;
        }
        sTabManager = this;

        mDummyTab = new DummyTab();
    }

    public void onActivityPause() {
        // Persist Tab state
        if (!persistState())
            Logger.info("TabManager.onActivityPause: Unable to persist Tab state");

        for (TabData td : mTabs)
            if (td.tab != null)
                td.tab.onActivityPause();
    }

    public void onActivityResume() {
        for (TabData td : mTabs)
            if (td.tab != null)
                td.tab.onActivityResume();
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

    //Implementing WebTab.ClientDelegate
    @Override
    public boolean addNewContents(int nativeSourceWebContents, int nativeWebContents,
                int disposition, Rect initialPosition, boolean userGesture) {
        //mActiveTab can be wrong in certain complex cases
        Intention args = new Intention(Type.I_OpenAndConsume);
        args.mNativeWebContents = nativeWebContents;
        args.mParent = mActiveTab;
        setTabIntention(args);
        return true;
    }


    public void setTabIntention(Intention args) {

        //sanity check
        Type type = args.mType;
        if (type != Type.I_Discover && type != Type.I_Consume
            && type != Type.I_Welcome && type != Type.I_OpenAndConsume) {
            Logger.notImplemented();
            return;
        }

        if (type == Type.I_Welcome) {
            createTab(Embodiment.E_Welcome, args);
            return;
        }

        // if no tabs, add a new one (web, by default)
        if (type == Type.I_OpenAndConsume || mTabs.isEmpty() || mActiveTab == null) {
            Logger.debug("setTabIntention: creating a new tab");
            createTab(Embodiment.E_Web, args);
        }

        // convert the current to a Web tab, if needed
        if (mActiveTab.tab.getEmbodiment() == Embodiment.E_Welcome) {
            if (!convertTabEmbodiment(mActiveTab, Embodiment.E_Web)) {
                Logger.apierror("Cannot convert the Tab to a Web tab");
                return;
            }
        }

        // the address to be used on the current tab
        String url = args.mUrl;

        // if it's a Discover intention, use the search engine
        if (type == Intention.Type.I_Discover && url != null && !url.contains(DEFAULT_SEARCH_QUERY_PREFIX))
            url = DEFAULT_SEARCH_QUERY_PREFIX + url;

        Logger.debug("Intention loading url: "+url + " td: " + mActiveTab.tab);

        // load the URL on the active tab
        if(url != null && !("".equals(url)))
          mActiveTab.tab.loadUrl(url);

        showTab(mActiveTab);
    }


    public void showTab(TabData td) {
        setActiveTab(td);
        for (Listener li : mListeners)
            li.onTabShow(td);

    }

    public void selectTab(TabData td) {
        setActiveTab(td);
    }

    private Timestamp getTimestamp() {
        Date d = new Date();
        return (new Timestamp(d.getTime()));
    }

    private TabData createTab(Embodiment e, Intention args) {
        TabData td = new TabData();

        Logger.debug("Creating TAB");

        int idx = mTabs.size();
        mTabs.add(idx, td);

        if (e == Embodiment.E_Welcome) {
            td.tab = new DummyTab();
        } else if (e == Embodiment.E_Web) {
            WebTab wtab = new WebTab(mActivity, args);
            wtab.setClientDelegate(this);

            if (args.mParent != null)
                td.parent = (TabData)args.mParent;

            td.tab = wtab;
        }

        if (td.tab == null) {
            Logger.debug("Failed creating tab of a new TabData");
            return null;
        }

        for (Listener li : mListeners)
            li.onTabAdded(td, idx);

        if (mActiveTab == null || args.mMakeActive)
            setActiveTab(td);

        return td;
    }

    public TabData validate(TabData tdata) {
        for (TabData td : mTabs)
            if (td == tdata)
                return td;
        return null;
    }

    public boolean hasParent(TabData td) {
        if (td == null || td.parent == null)
            return false;

        return true;
    }

    public boolean switchTab(boolean fwd) {

        if (mTabs.size() < 2 || mActiveTab == null)
           return false;

        int idx = mTabs.indexOf(mActiveTab);
        if (fwd)
            idx = (idx == (mTabs.size()-1)) ? 0 : idx+1;
        else
            idx = (idx == 0) ? (mTabs.size()-1) : idx-1;

        setActiveTab(mTabs.get(idx));
        return true;
    }

    public void closeTab(TabData td) {
        // remove the Tab, the tile will be removed as a consequence
        int location = mTabs.indexOf(td);
        if (location == -1) {
            Logger.debug("Tried to close an invalid tab");
            return;
        }

        TabData parent = validate(td.parent);

        mTabs.remove(location);

        for (Listener li : mListeners)
            li.onTabRemoved(td, location);

        if (parent != null) {
            setActiveTab(parent);
        } else {

            // move the active tab to the next one
            if (mActiveTab != null && mActiveTab == td) {
                int nextTab = location;
                if (nextTab >= mTabs.size())
                    nextTab = mTabs.size() - 1;
                if (nextTab < 0)
                    setActiveTab(null);
                else
                    setActiveTab(mTabs.get(nextTab));
            }
        }

        // perform deletion
        if (td.tab != null) {
            td.tab.destroy();
            td.tab = null;
        }
    }

    public int getTabsCount() {
        return mTabs.size();
    }

    public TabData getTabData(int i) {
        return mTabs.get(i);
    }

    // Added for testing
    public Tab getTab(int i) {
        return ((i >= 0 && i < getTabsCount()) ? mTabs.get(i).tab : null);
    }

    //Added for testing
    public int getTabParentIndex(TabData tabData) {
        return (tabData.parent == null ? -1 : mTabs.indexOf(tabData.parent));
    }

    public TabData getActiveTabData() {
        return mActiveTab;
    }

    public Tab getActiveTab() {
        //Must return a tab object
        if (mActiveTab != null && mActiveTab.tab != null) {
            return mActiveTab.tab;
        }

        return mDummyTab;
    }

    public int getActiveTabIndex() {
        return mActiveTab == null ? -1 : mTabs.indexOf(mActiveTab);
    }

    /**
     * Save the state of TabManager and all open WebTabs to a file
     */
    public boolean persistState() {
        FileOutputStream fos;
        JSONObject json, jsonTab;
        JSONArray jsonTabArray;
        Bitmap tabSnapshot;
        int snapshotWidth = mContext.getResources().getDimensionPixelSize(R.dimen.TilesSize);
        int snapshotHeight = snapshotWidth;
        String snapshotFilename;

        try {
            json = new JSONObject();
            int index, numTabs;
            byte[] tabState;

            numTabs = getTabsCount();

            // Don't store Tab state if there is only one tab and it is a Welcome tab
            if (numTabs == 1 && getActiveTab().getEmbodiment() == Embodiment.E_Welcome)
                numTabs = 0;

            // Store number of Tabs
            json.put(NUM_TABS_KEY, numTabs);

            if (numTabs != 0) {
                // Store activeTab index
                index = mTabs.indexOf(getActiveTabData());

                // If somehow activeTabIndex is invalid, set it to zero.
                json.put(ACTIVE_TAB_KEY, (index >= 0 && index < numTabs) ? index : 0);

                // Store Tabs state
                jsonTabArray = new JSONArray();
                for (TabData tabData: mTabs) {

                    jsonTab = new JSONObject();

                    // Store Tab embodiment
                    jsonTab.put(TAB_EMBODIMENT_KEY, tabData.tab.getEmbodiment().toString());

                    // Store Tab state
                    tabState = tabData.tab.getState();
                    jsonTab.put(TAB_STATE_KEY, (tabState == null) ? "" :
                        Base64.encodeToString(tabState, Base64.DEFAULT));

                    // Store Tab Title
                    jsonTab.put(TAB_TITLE_KEY, tabData.tab.getTitle());

                    // Store snapshot filename
                    snapshotFilename = (tabState == null) ? "" :
                        (TAB_SNAPSHOT_FILENAME_PREFIX + mTabs.indexOf(tabData) + ".png");
                    jsonTab.put(TAB_SNAPSHOT_FILE_KEY, snapshotFilename);

                    // Store index of Tab parent
                    jsonTab.put(TAB_PARENT_KEY, (tabData.parent == null) ? -1 :
                            mTabs.indexOf(tabData.parent.tab));

                    // add Tab state to Tab state array
                    jsonTabArray.put(jsonTab);

                    // If WebTab save snapshot file
                    if (tabState != null) {
                        tabSnapshot = tabData.tab.getSnapshot(snapshotWidth, snapshotHeight);
                        fos = mContext.openFileOutput(snapshotFilename, Context.MODE_PRIVATE);
                        if (fos != null) {
                            tabSnapshot.compress(Bitmap.CompressFormat.PNG, 0, fos);
                            fos.close();
                        }
                    }
                }
                // add Tab array to state
                json.put(TAB_ARRAY_KEY, jsonTabArray);
            }

            // Save state to the file
            fos = mContext.openFileOutput(TAB_STATE_FILENAME, Context.MODE_PRIVATE);
            if (fos != null) {
                fos.write(json.toString(4).getBytes());
                fos.close();
                Logger.info("TabManager.persistState: " + TAB_STATE_FILENAME +
                        " successfully saved");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Restore the state of all the tabs.
     * @param inState The saved state of all the tabs.
     * @return True if there were previous tabs that were restored. False if
     *         there was no saved state or restoring the state failed.
     */
    public boolean restoreState() {
        final int numTabs, activeTabIndex;
        JSONObject json, jsonTab;
        JSONArray jsonTabArray;
        byte[] tabState;
        int[] parentIndexes;
        TabData[] tabs;
        String embodiment, tabTitle, tabStateString, tabSnapshotFilename;
        Intention args;

        try {
            //Read JSONObject from file
            json = readJsonFromFile(TAB_STATE_FILENAME);

            if (json == null)
                return false;

            // Fetch number of Tabs from file
            numTabs = json.getInt(NUM_TABS_KEY);

            if (numTabs != 0) {
                // The following arrays are used to temporarily maintain parent
                // indexes and tabs until all tabs are created and the
                // parent-child relationships can be established
                parentIndexes = new int [numTabs];
                tabs = new TabData[numTabs];

                // Fetch activeTab index from file
                activeTabIndex = json.getInt(ACTIVE_TAB_KEY);

                // Fetch per Tab state from file
                jsonTabArray = json.getJSONArray(TAB_ARRAY_KEY);
                for (int i = 0; i < numTabs; i++) {
                    jsonTab = jsonTabArray.getJSONObject(i);

                    // Fetch Tab embodiment from file
                    embodiment = jsonTab.getString(TAB_EMBODIMENT_KEY);

                    // Fetch Tab state from file
                    tabStateString = jsonTab.getString(TAB_STATE_KEY);
                    tabState = tabStateString.isEmpty() ? null :
                        Base64.decode(tabStateString, Base64.DEFAULT);

                    // Fetch Tab title from file
                    tabTitle = jsonTab.getString(TAB_TITLE_KEY);

                    // Fetch snapshot filename from file
                    tabSnapshotFilename = jsonTab.getString(TAB_SNAPSHOT_FILE_KEY);

                    // Fetch index of Tab parent and store in array for
                    // restoration after all tabs have been created.
                    parentIndexes[i] = jsonTab.getInt(TAB_PARENT_KEY);

                    // Create a Tab
                    if (tabState == null) {
                        // Create a Dummy Tab
                        args = new Intention(Type.I_Welcome);
                        args.mMakeActive = (i == activeTabIndex);
                        tabs[i] = createTab(Embodiment.E_Welcome, args);
                    } else {
                        // Create a WebTab passing the restored State
                        args = new Intention(Type.I_OpenAndConsume);
                        args.mState = tabState;
                        args.mTitle = tabTitle;
                        args.mSnapshotFilename = tabSnapshotFilename;
                        args.mMakeActive = (i == activeTabIndex);
                        tabs[i] = createTab(Embodiment.E_Web, args);
                    }
                }
                // Now establish parent-child relations
                for (int i = 0; i < numTabs; i++) {
                    tabs[i].parent = (parentIndexes[i] == -1 ? null :
                        tabs[parentIndexes[i]]);
                }
            }
        return true;
        } catch (FileNotFoundException e){
            // Return true if there is no file to restore state from
            return true;
        } catch (JSONException e) {
            Logger.error("TabManager.restoreState: Unable to parse state file");
            return false;
        }
    }

    /** Private implementation ahead **/

    private void setActiveTab(TabData td) {
        if (mActiveTab == td) {
            return;
        }

        if (mActiveTab != null) {
            for (Listener li : mListeners)
                li.onTabSelected(mActiveTab, false);
        }

        mActiveTab = td;

        if (mActiveTab != null) {
            mActiveTab.timestamp = getTimestamp();
            for (Listener li : mListeners)
                li.onTabSelected(mActiveTab, true);
        }
    }

    private boolean convertTabEmbodiment(TabData td, Embodiment e) {

        if (td.tab.getEmbodiment() != Embodiment.E_Welcome || e != Embodiment.E_Web)
            return false;

        Intention args = new Intention(Type.I_Consume);
        args.mNativeWebContents = 0;
        WebTab wtab = new WebTab(mActivity, args);
        wtab.setClientDelegate(this);
        // copy setUseDesktopUserAgent setting
        wtab.setUseDesktopUserAgent(td.tab.getUseDesktopUserAgent());
        td.tab = wtab;

        for (Listener li : mListeners)
            li.onTabSelected(td, true);

        return true;
    }

    /*
     * @return JsonArray object from provided file or null if parsing failed.
     */
    private JSONObject readJsonFromFile(String TAB_STATE_FILENAME) throws FileNotFoundException {
        try {
            FileInputStream fis = mContext.openFileInput(TAB_STATE_FILENAME);
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();
            if (fis != null)
                fis.close();
            JSONTokener tokener = new JSONTokener(sb.toString());
            return new JSONObject(tokener);
        } catch (FileNotFoundException e) {
            throw e;
        }
        catch (IOException e) {
            Logger.error("TabManager.readJsonFromFile: Exception = " + e.toString());
            return null;
        } catch (JSONException e) {
            Logger.error("TabManager.readJsonFromFile: Exception = " + e.toString());
            return null;
        }
    }

    @CalledByNative
    static void createWebContentsDelegateImpl(int nativeWebContents) {
        TabManager tabManager = TabManager.getInstance();
        Intention args = new Intention(Type.I_OpenAndConsume,
                                       tabManager.mContext.getString(R.string.about_blank));
        args.mNativeWebContents = nativeWebContents;
        tabManager.setTabIntention(args);
    }

}
