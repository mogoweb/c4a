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

import com.mogoweb.browser.R;
import com.mogoweb.browser.views.smartbox.CompletionEntry.ActionType;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class has 2 main purposes:
 *  *
 *
 */
public class SearchModel {

    private static final boolean DEBUG_SEARCH_MODEL = false;
    private static final int MAX_QUERY_COMPLETIONS = 8;

    private final Context mContext;

    private String mLastText;
    private boolean mAttached;
    private CompletionAdapter mCompletionAdapter;

    private int mLastQueryIndex;

    private List<CompletionProvider> mCompletionProviders;


    public SearchModel(Context context) {
        mContext = context;
        mLastText = "";
        // mQueries = new ArrayList<SearchModel.PartialQuery>();
        mLastQueryIndex = -1;
    }

    public void setLastText(CharSequence text) {
        mLastText = text.toString();
    }

    public String getLastText() {
        return mLastText;
    }


    public void setAttached(boolean attached, List<CompletionProvider> completionProviders) {
        mAttached = attached;
        mCompletionProviders = attached ? completionProviders : null;
    }

    public boolean getAttached() {
        return mAttached;
    }


    public void smartSearchCompletions(CharSequence query, int keyCode) {
        mCompletionAdapter.mFilter.filter(query);
    }


    /** CompletionAdapter (::ListAdapter) implementation **/

    public CompletionAdapter getCompletionAdapter() {
        if (mCompletionAdapter == null)
            mCompletionAdapter = new CompletionAdapter();
        return mCompletionAdapter;
    }

    public class CompletionAdapter extends BaseAdapter implements Filterable {

        private List<CompletionEntry> mCompletions;
        private CompletionFilterThreaded mFilter;
        private final LayoutInflater mLayoutInflater;

        public CompletionAdapter() {
            mCompletions = new ArrayList<CompletionEntry>();
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        CompletionEntry getCompletion(int position) {
            if (position < 0 || position >= mCompletions.size())
                return null;
            return mCompletions.get(position);
        }


        /** ::BaseAdapter **/

        @Override
        public int getCount() {
            return mCompletions.size();
        }

        @Override
        public Object getItem(int position) {
            return mCompletions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CompletionEntry entry = mCompletions.get(position);

            View root;
            if (convertView instanceof LinearLayout)
                root = convertView;
            else
                root = mLayoutInflater.inflate(R.layout.smartbox_line_item, parent, false);

            ImageView vIconL = (ImageView)root.findViewById(android.R.id.icon1);
            ImageView vIconR = (ImageView)root.findViewById(android.R.id.icon2);
            TextView vText1 = (TextView)root.findViewById(android.R.id.text1);
            TextView vText2 = (TextView)root.findViewById(android.R.id.text2);

            String prettyLine = entry.prettyName;
            if (DEBUG_SEARCH_MODEL)
                prettyLine += " (" + entry.relevance + ")";
            vText1.setText(prettyLine);

            String urlLine = entry.actionUrl;
            vText2.setText(urlLine);
            vText2.setVisibility(entry.actionType == ActionType.T_Search ? View.GONE : View.VISIBLE);

            if (entry.actionDrawable != null)
                vIconL.setImageDrawable(entry.actionDrawable);

            if (entry.actionType == ActionType.T_Navigation)
                vIconR.setImageResource(R.drawable.sb_action_go);
            else if (entry.actionType == ActionType.T_Search)
                vIconR.setImageResource(R.drawable.sb_action_search);

            return root;
        }


        /** ::Filterable **/

        @Override
        public Filter getFilter() {
            if (mFilter == null)
                mFilter = new CompletionFilterThreaded();
            return mFilter;
        }

        private class CompletionFilterThreaded extends Filter {

            private CompletionEntry.RelevanceComparator mRelevanceComparator = new CompletionEntry.RelevanceComparator();

            @Override
            protected FilterResults performFiltering(CharSequence query) {
                // short circuit evaluation
                if (query == null || query.length() < 1)
                    return null;

                // build a list of completions by invoking all providers
                List<CompletionEntry> allCompletions = new ArrayList<CompletionEntry>();
                for (CompletionProvider p : mCompletionProviders) {
                    // don't complete from disabled providers
                    if (!p.isEnabled())
                        continue;
                    List<CompletionEntry> cl = p.complete(query, mLastQueryIndex,
                            mContext.getResources());
                    if (cl != null)
                        allCompletions.addAll(cl);
                }

                // sort the list by relevance
                Collections.sort(allCompletions, mRelevanceComparator);

                // remove duplicates (by {actionUrl, actionType}, with lower
                // relevance)
                for (int i = allCompletions.size() - 1; i > 0; i--) {
                    CompletionEntry after = allCompletions.get(i);
                    for (int j = i - 1; j >= 0; j--) {
                        CompletionEntry before = allCompletions.get(j);
                        // compare {actionUrl, actionType}
                        if (before.actionUrl.equals(after.actionUrl)
                                && before.actionType == after.actionType) {
                            allCompletions.remove(i);
                            break;
                        }
                    }
                }

                // cut the list up to N entries
                while (allCompletions.size() > MAX_QUERY_COMPLETIONS)
                    allCompletions.remove(MAX_QUERY_COMPLETIONS);

                // return the cleaned up list of candidates as query results
                FilterResults res = new FilterResults();
                res.values = allCompletions;
                res.count = allCompletions.size();
                return res;
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                // when the user clicks on a CompletionEntry visual
                // representation use its url, or the prettyname, as the
                // completion entry
                if (resultValue instanceof CompletionEntry) {
                    CompletionEntry c = (CompletionEntry)resultValue;
                    return (c.actionUrl != null && c.actionUrl.length() > 2) ? c.actionUrl : c.prettyName;
                }

                // fall back for exceptional cases
                return super.convertResultToString(resultValue);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // safety check
                if (results == null || results.values == null)
                    return;

                // use the new set as the adapter's model
                mCompletions = (List<CompletionEntry>)results.values;

                // notify about the changed contents
                if (mCompletions.size() > 0)
                    notifyDataSetChanged();
                else
                    notifyDataSetInvalidated();
            }

        } // CompletionFilterThreaded

    } // CompletionAdapter

}
