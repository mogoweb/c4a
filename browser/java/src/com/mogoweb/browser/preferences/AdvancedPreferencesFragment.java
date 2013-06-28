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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.mogoweb.browser.R;

public class AdvancedPreferencesFragment extends PreferenceFragment
    implements Preference.OnPreferenceChangeListener {

    private BrowserPreferences browserPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        browserPrefs = BrowserPreferences.getInstance();

        // Load the XML preferences file
        addPreferencesFromResource(R.layout.preference_advanced);

        //associate this class as the listener for advanced settings
        Preference e = findPreference(PreferenceKeys.PREF_ENABLE_JAVASCRIPT);
        e.setOnPreferenceChangeListener(this);

        e = findPreference(PreferenceKeys.PREF_BLOCK_POPUPS);
        e.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object objValue) {
        boolean prefValue;

        if (null != objValue) {
            if(pref.getKey().equals(PreferenceKeys.PREF_ENABLE_JAVASCRIPT)) {
                prefValue = (Boolean)objValue;
                browserPrefs.setPreference(PreferenceKeys.PREF_ENABLE_JAVASCRIPT, prefValue);
                return true;
            }

            if(pref.getKey().equals(PreferenceKeys.PREF_BLOCK_POPUPS)) {
                prefValue = (!(Boolean)objValue);
                browserPrefs.setPreference(PreferenceKeys.PREF_BLOCK_POPUPS, (prefValue));
                return true;
            }
        }

        return false;
    }

}