// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.mogoweb.plugindemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mogoweb.browser.addon.pub.ITestService;

public class DemoPluginService extends Service {
    private final static String TAG = "DemoPluginService";

    @Override
    public IBinder onBind(Intent intent) {
            Log.i(TAG, "onBind");
            return mTestBinder;
    }

    private final ITestService.Stub mTestBinder = new ITestService.Stub() {
        public int op(int i1, int i2) {
            return i1 * i2;
        }
    };
}
