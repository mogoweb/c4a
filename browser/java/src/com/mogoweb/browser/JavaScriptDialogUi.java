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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

/**
 * UI for JavaScript Dialogs (Alert, confirm & prompt ).
 */
public class JavaScriptDialogUi  {

    private final Context mContext;
    private final ImplInterface mImpl;

    /**
     * Abstraction to separate the implementation from the UI.
     */
    public static interface ImplInterface {
        public static final int OK = 1;
        public static final int CANCEL = 0;

        public void onResponse(int response, String value);
    }

    public JavaScriptDialogUi(Context context, ImplInterface obj) {
        mContext = context;
        mImpl = obj;
    }

    public void confirm(String title) {
        dialog(title, null, true);
    }

    public void alert(String title) {
        dialog(title, null, false);
    }

    public void prompt(String title, String promptValue) {
        dialog(title, promptValue, true);
    }

    public void dialog(String title, String promptValue, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final EditText input = new EditText(mContext);
        String lines[] = title.split("\\r?\\n");
        if (lines[0] != null){
            String dialogTitle = mContext.getResources().getString(R.string.js_dialog_title);
            builder.setTitle(String.format(dialogTitle, lines[0] ));
        }
        // Chrome does not let you dismiss the dialog following the same
        builder.setCancelable(false);
        if (lines[2] != null)
            builder.setMessage(lines[2]);

        if (promptValue != null) {
            // Set an EditText view to get user input
            input.setText(promptValue);
            builder.setView(input);
        }

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String value = null;
                if (input != null)
                    value = input.getText().toString();
                mImpl.onResponse(ImplInterface.OK, value);
            }
        });
        if (cancelable == true) {
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mImpl.onResponse(ImplInterface.CANCEL, null);
                }
            });
        }
        builder.show();
    }

}
