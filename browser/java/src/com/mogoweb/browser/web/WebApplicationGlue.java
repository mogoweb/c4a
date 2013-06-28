/*
 *  Copyright (c) 2012,2013 The Linux Foundation. All rights reserved.
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

package com.mogoweb.browser.web;

import org.chromium.base.PathUtils;
import org.chromium.content.app.LibraryLoader;
import org.chromium.content.browser.ActivityContentVideoViewDelegate;
import org.chromium.content.browser.AndroidBrowserProcess;
import org.chromium.content.browser.ContentVideoView;
import org.chromium.content.browser.ContentView;
import org.chromium.content.browser.DeviceUtils;
import org.chromium.content.browser.ResourceExtractor;
import org.chromium.content.common.ProcessInitException;
import org.chromium.ui.WindowAndroid;

import com.mogoweb.browser.BrowserApplication;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

public class WebApplicationGlue {

    private final static String LOGTAG = "browser";

    // Set to true to enable verbose logging.
    final static boolean LOGV_ENABLED = false;

    // Set to true to enable extra debug logging.
    final static boolean LOGD_ENABLED = true;

    private static final String TAG = BrowserApplication.class.getCanonicalName();
    private static final String PRIVATE_DATA_DIRECTORY_SUFFIX = "mogo";
    private static final String[] CHROME_MANDATORY_PAKS = {
        "chrome.pak",
        "en-US.pak",
        "resources.pak",
        "chrome_100_percent.pak",
    };

//    private static final String[] NATIVE_LIBRARIES = new String[] {
//        /* Custom V8 library. This will be loaded first, to make it possible to
//         * resolve the main library. the name should match one in swe/swe.gypi
//         */
//        "swev8",
//        /* Main library */
//        "content_swe_browser",
//        /* Optional Network library */
//        "netxt_plugin_proxy",
//    };
//    private static final String[] MANDATORY_PAK_FILES = new String[] {"swe_browser.pak"};
//    private static final String PRIVATE_DATA_DIRECTORY_SUFFIX = "swe_browser";

    private static boolean sInitialized = false;
    private static WindowAndroid sWindowAndroid;

    /**
     * Engine initialization at the application level. Handles initialization of
     * information that needs to be shared across the main activity and the
     * sandbox services created.
     *
     * @param activity
     */
    public static void ensureNativeSWEInitialized(Activity activity) {
        if (sInitialized)
            return;
        sInitialized = true;

        // Extract the PAK files
        ResourceExtractor.setMandatoryPaksToExtract(CHROME_MANDATORY_PAKS);

        // Sync app data path
        PathUtils.setPrivateDataDirectorySuffix(PRIVATE_DATA_DIRECTORY_SUFFIX);

        // Create the native window proxy
        sWindowAndroid = new WindowAndroid(activity);

        // Setup the user agent
        DeviceUtils.addDeviceSpecificUserAgentSwitch(activity);

        // Initialize the chromium process
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                AndroidBrowserProcess.init(activity, AndroidBrowserProcess.MAX_RENDERERS_AUTOMATIC);
            else {
                AndroidBrowserProcess.init(activity, AndroidBrowserProcess.MAX_RENDERERS_SINGLE_PROCESS);
            }
        } catch (ProcessInitException e) {
            Log.e(TAG, "Chromium browser process initialization failed", e);
        }

        // Setup the video view
        ContentVideoView.registerContentVideoViewContextDelegate(new ActivityContentVideoViewDelegate(activity));
    }

    /**
     * Used by classes that want to know whether the engine was initialized
     * (libraries loaded, engine set-up) or not.
     *
     * @return true if the web.* classes are usable.
     */
    public static boolean getIsInitialized() {
        return sInitialized;
    }

    public static WindowAndroid getWindowAndroid() {
        return sWindowAndroid;
    }

}
