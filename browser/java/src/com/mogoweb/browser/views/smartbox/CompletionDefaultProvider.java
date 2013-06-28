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

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

public class CompletionDefaultProvider extends CompletionProvider {

    public CompletionDefaultProvider() {
    }

    @Override
    public String getProviderName() {
        return "Local";
    }

    @Override
    public List<CompletionEntry> complete(CharSequence query, int queryIdx, Resources r) {
        List<CompletionEntry> l = new ArrayList<CompletionEntry>();

        // The first entry is a 'search' for that query
        CompletionEntry qS = new CompletionEntry();
        qS.inQuery = query;
        qS.inQueryIdx = queryIdx;
        qS.prettyName = "Search for '" + query + "' ?";
        qS.actionUrl = query.toString();
        qS.actionType = ActionType.T_Search;
        qS.relevance = CompletionEntry.STANDARD_SEARCH_RELEVANCE;
        qS.actionDrawable = r.getDrawable(R.drawable.sb_provider_search);
        l.add(qS);

        // The second entry is an URL completer.
        // TODO: make this more intelligent and complete more things
        if (!query.toString().contains(".") && !query.toString().contains(" ")) {
            CompletionEntry wC = new CompletionEntry();
            wC.inQuery = query;
            wC.inQueryIdx = queryIdx;
            wC.prettyName = query + ".com";
            wC.actionUrl = "http://www." + query + ".com/";
            wC.actionType = ActionType.T_Navigation;
            wC.relevance = CompletionEntry.STANDARD_CAST_RELEVANCE;
            wC.actionDrawable = null;
            l.add(wC);
        }

        // TODO: hook into Favorites and Bookmarks and History

        return l;
    }

}
