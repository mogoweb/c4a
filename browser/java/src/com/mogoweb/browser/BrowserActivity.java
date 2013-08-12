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

import org.chromium.chrome.browser.ApplicationLifetime;
import org.chromium.content.common.CommandLine;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;

import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.web.WebApplicationGlue;

public class BrowserActivity extends Activity
        implements ApplicationLifetime.Observer {

    public static final String COMMAND_LINE_FILE = "/data/local/tmp/swe-browser-command-line";
    public static final String COMMAND_LINE_ARGS_KEY = "commandLineArgs";
    public static final String COMMAND_LINE_ENABLE_GPU_BENCHMARKING = "enable-gpu-benchmarking";

    private BrowserUi mUi;

    private Boolean mRestartFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRestartFlag = false;

        // Setting up handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler.create(this));

        // Initiating Crash Upload
        CrashHandler.handleServerUpload();

        // Initializing the command line must occur before loading the library.
        if (!CommandLine.isInitialized()) {
            CommandLine.initFromFile(COMMAND_LINE_FILE);

            String[] commandLineParams = getCommandLineParamsFromIntent(getIntent());
            if (commandLineParams != null) {
                CommandLine.getInstance().appendSwitchesAndArguments(commandLineParams);
            }
        }

        // enable webgl by default
        CommandLine.getInstance().appendSwitch(CommandLine.ENABLE_WEBGL);

        waitForDebuggerIfRequested();

        // initialize web engine
        WebApplicationGlue.ensureNativeSWEInitialized(this);

        // create the UI. This operations should be as lightweight as possible, so that we can
        // "display something" as soon as possible while leaving all the initialization for
        // later. Loading the libraries for the WebEngine shouldn't have happened yet.
        mUi = new BrowserUi(this);

        // Restore Tabs state
        if (!TabManager.getInstance().restoreState())
            Logger.info("SRS_BrowserUi.handleIntent: Unable to restore Tab state");

        // If we are going to background we should not create another tab.
        if (!mUi.handleIntent(getIntent()) && TabManager.getInstance().getTabsCount() == 0)
            mUi.createNewWelcomeTab();

        ApplicationLifetime.setObserver(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mUi.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mUi.onResume();
    };

    @Override
    protected void onPause() {
        super.onPause();

        mUi.onPause();
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUi.onDestroy();

        if (mRestartFlag)
            System.exit(0);
    };

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        mUi.handleIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // TODO: add code to handle more than just a layout change
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        mUi.onCreateContextMenu(menu, view, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return mUi.onContextItemSelected(item);
     }

    @Override
    public void onBackPressed() {
        if (mUi.handleBackButton() == false) {
           moveTaskToBack(true);
        }
    }


    private static String[] getCommandLineParamsFromIntent(Intent intent) {
        return intent != null ? intent.getStringArrayExtra(COMMAND_LINE_ARGS_KEY) : null;
    }

    public static boolean requestedGpuBenchmarkMode() {
        return CommandLine.getInstance().hasSwitch(COMMAND_LINE_ENABLE_GPU_BENCHMARKING);
    }

    private static void waitForDebuggerIfRequested() {
        if (CommandLine.getInstance().hasSwitch(CommandLine.WAIT_FOR_JAVA_DEBUGGER)) {
            Logger.info("Waiting for Java debugger to connect...");
            android.os.Debug.waitForDebugger();
            Logger.info("Java debugger connected. Resuming execution.");
        }
    }

    // implementation of ApplicationLifetime.Observer interface
    @Override
    public void onTerminate(boolean restart) {
        if (restart) {
            Intent i = new Intent(this, BrowserActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManger = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            long triggerAtMillis = System.currentTimeMillis() + 3000; // 3s
            alarmManger.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);

            mRestartFlag = true;
            finish();
        }
    }
}
