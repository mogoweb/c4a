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

package com.mogoweb.browser.views.smartbox;

import com.mogoweb.browser.Intention;
import com.mogoweb.browser.views.smartbox.SearchModel.CompletionAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;


/**
 * The SmartBox shows the suggestions as-you-type. These suggestions
 * come from different providers (local, search engines, etc).
 *
 * This class needs a SearchModel to be plugged in to operate, or
 * otherwise it just behaves like a normal text edit box.
 */
public class SmartBox extends AutoCompleteTextView {

    private List<CompletionProvider> mSearchProviders;
    private SearchModel mSearchModel;

    public SmartBox(Context context) {
        super(context);
        init();
    }

    public SmartBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmartBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        // Create the completion providers.
        mSearchProviders = new ArrayList<CompletionProvider>();
        mSearchProviders.add(new CompletionDefaultProvider());
        mSearchProviders.add(new CompletionSearchGoogleProvider());

        // Apply initial enabled/disabled state
        // TODO: enable/disable them runtime
        mSearchProviders.get(0).setEnabled(true);
        mSearchProviders.get(1).setEnabled(true);
    }

    public void setSearchModel(SearchModel searchModel) {
        if (mSearchModel != null)
            mSearchModel.setAttached(false, null);

        mSearchModel = searchModel;

        if (mSearchModel == null)
            return;

        // reconnect and display the provided SearchModel
        mSearchModel.setAttached(true, mSearchProviders);
        setAdapter(mSearchModel.getCompletionAdapter());
        super.setText(mSearchModel.getLastText());
    }

    public Intention getIntentionFromPopupPosition(int position) {
        // return the intention from the completions in the model
        if (mSearchModel != null) {
            CompletionAdapter adapter = mSearchModel.getCompletionAdapter();
            CompletionEntry ce = adapter.getCompletion(position);
            if (ce == null)
                return null;

            // located the completion, now generate the correct intention
            switch (ce.actionType) {
                case T_Navigation:
                    return new Intention(Intention.Type.I_Consume, ce.actionUrl);

                case T_Search:
                    String query = ce.actionUrl;
                    if (query == null || query.length() < 2)
                        query = ce.prettyName;
                    return new Intention(Intention.Type.I_Discover, query);
            }
        }
        return null;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        // record the current text on the SearchModel
        if (mSearchModel != null)
            mSearchModel.setLastText(text);
        super.setText(text, type);
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        // This function is invoked every time something is typed on
        // the edit box. The base implementation will filter the
        // contents of the static-contents adapter that is plugged
        // into the pop-up, but we intercept the implementation to
        // generate dynamic contents instead.

        // Only operate if the popup is showing, i.e. we have focus
        // and the user is interacting. This prevents search from
        // being invoked by other setText operations, like unwinding
        // the URL stack (back button).
        if (!hasFocus())
            return;

        if (mSearchModel != null)
            mSearchModel.smartSearchCompletions(text, keyCode);

        // Note, we don't need any of these.. but keep this here in
        // case it's needed to debug (exp onFilterComplete(1))
        // getFilter().filter(text, this);
        // super.performFiltering(text, keyCode);
        // onFilterComplete(1);
    }

}
