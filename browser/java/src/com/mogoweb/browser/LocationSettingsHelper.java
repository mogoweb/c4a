// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.mogoweb.browser;

import org.chromium.base.CalledByNative;

import com.mogoweb.browser.preferences.BrowserPreferences;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

public class LocationSettingsHelper {

    private static LocationSettingsHelper sInstance;
    private Context mContext;

    @CalledByNative
    public static LocationSettingsHelper getInstance(Context context) {
        if (sInstance == null)
            sInstance = new LocationSettingsHelper(context);
        return sInstance;
    }

    private LocationSettingsHelper(Context context) {
        mContext = context;
    }

    @CalledByNative
    public String getAcceptButtonLabel() {
        return "allow";
    }

    @CalledByNative
    public void showGoogleLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        mContext.startActivity(intent);
    }

    @CalledByNative
    public boolean isGoogleAppsLocationSettingEnabled() {
        return true;
    }

    @CalledByNative
    public boolean isMasterLocationSettingEnabled() {
//        LocationManager locationManager = (LocationManager) (mContext.getSystemService(Context.LOCATION_SERVICE));
//
//        if (locationManager != null
//                && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                        || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
//            return true;
//        } else {
//            return false;
//        }
        return BrowserPreferences.getInstance().getGeolocationEnabled();
    }
}
