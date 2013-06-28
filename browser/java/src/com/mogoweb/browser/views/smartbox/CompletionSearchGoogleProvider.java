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
import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.views.smartbox.CompletionEntry.ActionType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

public class CompletionSearchGoogleProvider extends CompletionHttpProvider {

    private static final String SEARCH_PATTERN = "http://www.google.com/complete/search?client=$CLIENT&q=$QUERY&hl=$LANG";

    public CompletionSearchGoogleProvider() {
        super(1);
    }

    @Override
    public String getProviderName() {
        return "Google Search";
    }

    @Override
    public List<CompletionEntry> complete(CharSequence query, int queryIdx, Resources r) {

        String serviceUrl = prepareServiceQuery(SEARCH_PATTERN, query.toString());
        if (serviceUrl == null)
            return null;

        String responseContent;
        try {
            responseContent = getHttpResponse(serviceUrl);
        } catch (Exception e) {
            return null;
        }

        // Parse the response content
        if (responseContent == null || responseContent.length() < 3) {
            Logger.info("CompletionSearchGoogleProvider: invalid JSON response");
            return null;
        }

        // parse the JSON response
        try {
            JSONTokener jsonTokener = new JSONTokener(responseContent);
            Object value = jsonTokener.nextValue();

            // do the parsing of the array here
            if (value instanceof JSONArray)
                return parseGoogleSearchJson((JSONArray)value, query, queryIdx, r);

        } catch (JSONException e) {}

        // error parsing the response
        Logger.info("CompletionSearchGoogleProvider: error parsing the JSON response ("
                + responseContent + ")");
        return null;
    }

    private List<CompletionEntry> parseGoogleSearchJson(JSONArray array, CharSequence query,
            int queryIdx, Resources r) throws JSONException {
        if (array.length() < 5) {
            Logger.info("parseGoogleSearchJson: invalid length");
            return null;
        }

        String rQuery = array.getString(0);
        if (rQuery.compareToIgnoreCase(query.toString()) != 0) {
            Logger.debug("parseGoogleSearchJson: query mismatch");
        }
        JSONArray rCUrl = array.getJSONArray(1);
        JSONArray rCName = array.getJSONArray(2);
        // JSONArray rC3 = array.getJSONArray(3);
        JSONObject rAttrs = array.getJSONObject(4);

        JSONArray rAttrType = rAttrs.has("google:suggesttype") ? rAttrs.getJSONArray("google:suggesttype") : null;
        JSONArray rAttrRelevance = rAttrs.has("google:suggestrelevance") ? rAttrs.getJSONArray("google:suggestrelevance") : null;

        int entries = rCUrl.length();
        ArrayList<CompletionEntry> l = new ArrayList<CompletionEntry>();
        for (int i = 0; i < entries; i++) {
            CompletionEntry ce = new CompletionEntry();
            ce.inQuery = query;
            ce.inQueryIdx = queryIdx;
            ce.prettyName = rCName.getString(i);
            ce.actionUrl = rCUrl.getString(i);
            ce.actionType = ActionType.T_Search;
            ce.relevance = CompletionEntry.STANDARD_EXTERNAL_RELEVANCE;
            ce.actionDrawable = r.getDrawable(R.drawable.sb_provider_google);

            if (rAttrType != null && rAttrType.length() > i) {
                String type = rAttrType.getString(i);
                if ("NAVIGATION".equals(type))
                    ce.actionType = ActionType.T_Navigation;
                else if ("QUERY".equals(type))
                    ce.actionType = ActionType.T_Search;
                else
                    Logger.info("parseGoogleSearchJson: Parsing of type " + type + " not implemented.");
            }

            if (rAttrRelevance != null && rAttrRelevance.length() > i) {
                int relevance = rAttrRelevance.getInt(i);
                if (relevance < 0)
                    relevance = 0;
                else if (relevance > 1000)
                    relevance = 1000;
                ce.relevance = relevance;
            }

            // use the action as the prettyName if no prettyName was present
            if (ce.prettyName == null || ce.prettyName.isEmpty())
                ce.prettyName = ce.actionUrl;

            // we have a completion entry!
            l.add(ce);
        }

        return l;
    }

}
