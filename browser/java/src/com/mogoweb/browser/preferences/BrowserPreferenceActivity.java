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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.EditText;

import com.mogoweb.browser.BrowsingDataRemover;
import com.mogoweb.browser.R;

public class BrowserPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private BrowserPreferences browserPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        browserPrefs = BrowserPreferences.getInstance();

        addPreferencesFromResource(R.xml.preference_browser);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        Preference button_clear_browsing_data = (Preference)findPreference("clear_browsing_data");
        button_clear_browsing_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                onClickClearBrowsingData();

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PreferenceKeys.PREF_ENABLE_JAVASCRIPT)) {
            boolean prefValue = sharedPreferences.getBoolean(key, true);
            browserPrefs.setPreference(PreferenceKeys.PREF_ENABLE_JAVASCRIPT, prefValue);
        } else if (key.equals(PreferenceKeys.PREF_BLOCK_POPUPS)) {
            boolean prefValue = sharedPreferences.getBoolean(key, true);
            browserPrefs.setPreference(PreferenceKeys.PREF_BLOCK_POPUPS, prefValue);
        } else if (key.equals(PreferenceKeys.PREF_USER_AGENT)) {
            String prefValue = sharedPreferences.getString(key, "");
            browserPrefs.setPreference(key, prefValue);
        }
    }

    boolean[] initSelected = new boolean[] {true, true, true, false};
    boolean[] selected = new boolean[] {true, true, true, false};
    private void onClickClearBrowsingData() {
        Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.clear_browsing_data_title);

        DialogInterface.OnMultiChoiceClickListener listener =
            new DialogInterface.OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface,
                        int which, boolean isChecked) {
                    selected[which] = isChecked;
                }
            };
        builder.setMultiChoiceItems(R.array.clear_browsing_data_items, initSelected, listener);
        DialogInterface.OnClickListener clearListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    for (int i = 0; i < selected.length; i++) {
                        if (selected[i]) {
                            BrowsingDataRemover.clearData(i);
                        }
                    }
                }
            };
        DialogInterface.OnClickListener cancelListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.cancel();
                }
            };
        builder.setPositiveButton(R.string.clear, clearListener);
        builder.setNegativeButton(R.string.cancel, cancelListener);
        Dialog dialog = builder.create();
        dialog.show();
    }
}