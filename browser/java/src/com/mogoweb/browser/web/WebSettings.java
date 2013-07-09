// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.mogoweb.browser.web;

import org.chromium.base.JNINamespace;
import org.chromium.base.ThreadUtils;

import com.mogoweb.browser.preferences.BrowserPreferences;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

/**
 * Stores specific settings that does not need to be synced to WebKit.
 * Use {@link org.chromium.content.browser.ContentSettings} for WebKit settings.
 *
 * Methods in this class can be called from any thread.
 */
public class WebSettings {

    // This class must be created on the UI thread. Afterwards, it can be
    // used from any thread. Internally, the class uses a message queue
    // to call native code on the UI thread only.

    // Lock to protect all settings.
    private final Object mWebSettingsLock = new Object();

    private final Context mContext;

    private boolean mJavaScriptEnabled = false;
    private boolean mJavaScriptCanOpenWindowsAutomatically = false;

    // Not accessed by the native side.
    private boolean mBlockNetworkLoads;  // Default depends on permission of embedding APK.

    // The native side of this object.
    private int mNativeWebSettings = 0;

    // A flag to avoid sending superfluous synchronization messages.
    private boolean mIsUpdateWebkitPrefsMessagePending = false;
    // Custom handler that queues messages to call native code on the UI thread.
    private final EventHandler mEventHandler;

    // Class to handle messages to be processed on the UI thread.
    private class EventHandler {
        // Message id for updating Webkit preferences
        private static final int UPDATE_WEBKIT_PREFERENCES = 0;
        // Actual UI thread handler
        private Handler mHandler;

        EventHandler() {
            mHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case UPDATE_WEBKIT_PREFERENCES:
                                synchronized (mWebSettingsLock) {
                                    updateWebkitPreferencesOnUiThread();
                                    mIsUpdateWebkitPrefsMessagePending = false;
                                    mWebSettingsLock.notifyAll();
                                }
                                break;
                        }
                    }
                };
        }

        private void updateWebkitPreferencesLocked() {
            assert Thread.holdsLock(mWebSettingsLock);
            if (mNativeWebSettings == 0) return;
            if (Looper.myLooper() == mHandler.getLooper()) {
                updateWebkitPreferencesOnUiThread();
            } else {
                // We're being called on a background thread, so post a message.
                if (mIsUpdateWebkitPrefsMessagePending) {
                    return;
                }
                mIsUpdateWebkitPrefsMessagePending = true;
                mHandler.sendMessage(Message.obtain(null, UPDATE_WEBKIT_PREFERENCES));
                // We must block until the settings have been sync'd to native to
                // ensure that they have taken effect.
                try {
                    while (mIsUpdateWebkitPrefsMessagePending) {
                        mWebSettingsLock.wait();
                    }
                } catch (InterruptedException e) {}
            }
        }
    }

    public WebSettings(Context context, int nativeWebContents,
            boolean isAccessFromFileURLsGrantedByDefault) {
        ThreadUtils.assertOnUiThread();
        mContext = context;

        mBlockNetworkLoads = mContext.checkPermission(
                android.Manifest.permission.INTERNET,
                Process.myPid(),
                Process.myUid()) != PackageManager.PERMISSION_GRANTED;
        mNativeWebSettings = nativeInit(nativeWebContents);
        assert mNativeWebSettings != 0;

        mEventHandler = new EventHandler();
        nativeUpdateEverything(mNativeWebSettings);
    }

    public void destroy() {
        nativeDestroy(mNativeWebSettings);
        mNativeWebSettings = 0;
    }

    public void setWebContents(int nativeWebContents) {
        synchronized (mWebSettingsLock) {
            nativeSetWebContents(mNativeWebSettings, nativeWebContents);
        }
    }

    /**
     * See {@link android.webkit.WebSettings#setJavaScriptEnabled}.
     */
    public void setJavaScriptEnabled(boolean flag) {
        synchronized (mWebSettingsLock) {
            if (mJavaScriptEnabled != flag) {
                mJavaScriptEnabled = flag;
                mEventHandler.updateWebkitPreferencesLocked();
            }
        }
    }

    /**
     * See {@link android.webkit.WebSettings#setJavaScriptCanOpenWindowsAutomatically}.
     */
    public void setJavaScriptCanOpenWindowsAutomatically(boolean flag) {
        synchronized (mWebSettingsLock) {
            if (mJavaScriptCanOpenWindowsAutomatically != flag) {
                mJavaScriptCanOpenWindowsAutomatically = flag;
                mEventHandler.updateWebkitPreferencesLocked();
            }
        }
    }

    private void updateWebkitPreferencesOnUiThread() {
        if (mNativeWebSettings != 0) {
            ThreadUtils.assertOnUiThread();
            nativeUpdateWebkitPreferences(mNativeWebSettings);
        }
    }

    private native int nativeInit(int webContentsPtr);

    private native void nativeDestroy(int nativeWebSettings);

    private native void nativeSetWebContents(int nativeWebSettings, int nativeWebContents);

    private native void nativeUpdateEverything(int nativeWebSettings);

    private native void nativeUpdateWebkitPreferences(int nativeWebSettings);
}
