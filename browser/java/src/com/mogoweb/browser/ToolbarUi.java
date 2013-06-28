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

import com.mogoweb.browser.Intention.Type;
import com.mogoweb.browser.views.smartbox.SearchModel;
import com.mogoweb.browser.views.smartbox.SmartBox;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.regex.Pattern;


public class ToolbarUi implements TextView.OnEditorActionListener, View.OnClickListener, View.OnFocusChangeListener, AdapterView.OnItemClickListener {

    private final Context mContext;

    private final SmartBox mSmartBox;
    private final SearchModel mSmartBoxSearchModel;
    private final View mUrlSearchHint;
    private final ImageButton mButtonHome;
    private final ImageButton mButtonOverflow;
    private final View mUrlPageGroup;
    private final ImageButton mUrlPageStop;
    private final ImageButton mUrlPageReload;
    private final ImageButton mUrlClear;
    private final Drawable mProgressDrawable;

    private Listener mListener;
    private boolean mIsLoading;

    interface Listener {

        /**
         * ToolBar is requesting to load a new intention on the current tab.
         * This corresponds to the user entering a URL.
         */
        void onToolbarPageSetIntention(Intention i);

        /**
         * Stops the page that is being loaded.
         */
        void onToolbarPageStop();

        /**
         * Reloads the page that was loaded.
         */
        void onToolbarPageReload();

        /**
         * ToolBar is requesting for the Home screen to be shown. This reflects
         * the click on the Home button.
         */
        void onToolbarToggleHome();

        /**
         * ToolBar is requesting for the Overflow Menu to be shown. This
         * reflects the click on the Overflow button.
         */
        void onToolbarToggleOverflowMenu();

    }

    public ToolbarUi(View rootView, Context ctx) {
        mContext = ctx;

        mSmartBox = (SmartBox) rootView.findViewById(R.id.toolbar_url_editor);
        mUrlSearchHint = rootView.findViewById(R.id.toolbar_url_hint);
        mButtonHome = (ImageButton) rootView.findViewById(R.id.toolbar_btn_home);
        mButtonOverflow = (ImageButton) rootView.findViewById(R.id.toolbar_btn_overflow);
        mUrlPageGroup = rootView.findViewById(R.id.toolbar_url_loader);
        mUrlPageStop = (ImageButton) rootView.findViewById(R.id.toolbar_url_stop);
        mUrlPageReload = (ImageButton) rootView.findViewById(R.id.toolbar_url_reload);
        mUrlClear = (ImageButton) rootView.findViewById(R.id.toolbar_url_clear);
        mProgressDrawable = rootView.findViewById(R.id.toolbar_progress).getBackground();

        // use a shared search model for the SmartBox. note that we could have
        // one-per-tab, or a different one for the_Incognito mode
        mSmartBoxSearchModel = new SearchModel(mContext);
        mSmartBox.setSearchModel(mSmartBoxSearchModel);

        // listen to clicks and actions
        mUrlSearchHint.setOnClickListener(this);
        mButtonHome.setOnClickListener(this);
        mButtonOverflow.setOnClickListener(this);
        mUrlPageStop.setOnClickListener(this);
        mUrlPageReload.setOnClickListener(this);
        mUrlClear.setOnClickListener(this);

        mSmartBox.setOnEditorActionListener(this);
        mSmartBox.setOnFocusChangeListener(this);
        mSmartBox.setOnItemClickListener(this);

        setCurrentProgress(0);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setCurrentUrl(String url) {
        if (url == null) {
            mSmartBox.clearComposingText();
            mSmartBox.setText("");
        } else {
            mSmartBox.setText(url);

            // Color of the URL, for greater intelligibility
            // (this code is in beta...)
            /*final int length = url.length();
            int end = 0;
            if (url.startsWith("http://www."))
                end = 11;
            else if (url.startsWith("www."))
                end = 4;
            else if (url.startsWith("http://"))
                end = 7;
            int start = 0;
            if (url.endsWith("/"))
                start = length - 1;
            Editable colorizer = mUrlEdit.getText();
            colorizer.clearSpans();
            if (end > 0)
                colorizer.setSpan(new ForegroundColorSpan(Color.rgb(192, 192, 192)), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (start > 0)
                colorizer.setSpan(new ForegroundColorSpan(Color.rgb(192, 192, 192)), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            */
        }
        updateControlsVisibility();
    }

    public void setCurrentIsLoading(boolean isLoading) {
        mIsLoading = isLoading;
        updateControlsVisibility();
    }

    public void setCurrentProgress(int progress) {
        mProgressDrawable.setLevel(100 * progress);
    }

    public void setHomeButtonPressed(boolean pressed) {
        mButtonHome.setClickable(true);
        mButtonHome.setImageResource(pressed ? R.drawable.ic_menu_home_on : R.drawable.ic_menu_home);
    }

    public void setHomeButtonDisabled(boolean disabled) {
        mButtonHome.setClickable(!disabled);
        mButtonHome.setImageResource(disabled ? R.drawable.ic_menu_home_off : R.drawable.ic_menu_home_on);
    }

    public View getMenuAnchor() {
        return mButtonOverflow;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        final Pattern SWE_URL =
            Pattern.compile("(?i)(?:http|https|file|ftp|data|javascript|about|chrome):(.*)");

        // the user committed an edit operation (pressed enter) on the
        // SmartBox: execute the URL
        if (v.getId() == R.id.toolbar_url_editor && mListener != null) {

            String query = mSmartBox.getText().toString();
            if (query.isEmpty())
                return true;

            boolean isHost = android.util.Patterns.WEB_URL.matcher(query).matches()
                          || SWE_URL.matcher(query).matches();

            // the user just committed to a new navigation operation
            Intention i = new Intention(isHost ? Type.I_Consume : Type.I_Discover, query);

            // handle the intention
            mListener.onToolbarPageSetIntention(i);
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // the user committed to one of the completions
        Intention i = mSmartBox.getIntentionFromPopupPosition(position);

        // handle the intention
        if (i != null && mListener != null)
            mListener.onToolbarPageSetIntention(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.toolbar_url_hint:
            mSmartBox.requestFocus();
            return;

        case R.id.toolbar_btn_home:
            if (mListener != null)
                mListener.onToolbarToggleHome();
            break;

        case R.id.toolbar_btn_overflow:
            if (mListener != null)
                mListener.onToolbarToggleOverflowMenu();
            break;

        case R.id.toolbar_url_stop:
            if (mListener != null)
                mListener.onToolbarPageStop();
            break;

        case R.id.toolbar_url_reload:
            if (mListener != null)
                mListener.onToolbarPageReload();
            break;

        case R.id.toolbar_url_clear:
            setCurrentUrl(null);
            break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // when the text edit loses focus, hide any keyboard attached
        if (!hasFocus) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(mSmartBox.getWindowToken(), 0);
        } else {
            //mUrlEdit.getText().clearSpans();
        }
        updateControlsVisibility();
    }

    private void updateControlsVisibility() {
        boolean hasFocus = mSmartBox.isFocused();
        boolean hasText = mSmartBox.getText().length() > 0;

        mUrlSearchHint.setVisibility((!hasFocus && !hasText) ? View.VISIBLE : View.GONE);
        mUrlClear.setVisibility(hasFocus ? View.VISIBLE : View.GONE);

        boolean notEditing = !hasFocus && hasText;
        mUrlPageGroup.setVisibility(notEditing ? View.VISIBLE : View.GONE);
        mUrlPageStop.setVisibility((notEditing && mIsLoading) ? View.VISIBLE : View.GONE);
        mUrlPageReload.setVisibility((notEditing && !mIsLoading) ? View.VISIBLE : View.GONE);
    }

}
