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

import android.graphics.drawable.Drawable;

import java.util.Comparator;


/**
 * A guessed match for what the User is looking for.
 */
public class CompletionEntry {

    public static final int STANDARD_EXTERNAL_RELEVANCE = 400;
    public static final int STANDARD_SEARCH_RELEVANCE = 500;
    public static final int STANDARD_CAST_RELEVANCE = 600;

    public enum ActionType {
        T_Search, T_Navigation
    }

    // original query
    public CharSequence inQuery;    // The original query
    public int inQueryIdx;          // A progressive index, to remove outdated results

    // result
    public String prettyName;       // The name to be displayed
    public ActionType actionType;   // The type of the action to be taken
    public String actionUrl;        // The action to be taken if clicked
    public int relevance;           // 0 ... 1000
    public Drawable actionDrawable; // An additional image representation of the result

    // used for relevance-based sorting operations
    public static class RelevanceComparator implements Comparator<CompletionEntry> {

        @Override
        public int compare(CompletionEntry lhs, CompletionEntry rhs) {
            return rhs.relevance - lhs.relevance;
        }

    }

}
