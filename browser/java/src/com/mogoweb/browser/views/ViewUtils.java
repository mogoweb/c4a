/*
 *  Copyright (c) 2012, The Linux Foundation. All rights reserved.
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


package com.mogoweb.browser.views;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class ViewUtils {

    public static boolean removeViewFromParent(View view) {
        ViewParent viewParent = view.getParent();
        if (viewParent == null)
            return false;
        if (viewParent instanceof ViewGroup) {
            ((ViewGroup) viewParent).removeView(view);
            return true;
        }
        return false;
    }

    public static void toggleViewVisibility(View root, int viewId) {
        View view = root.findViewById(viewId);
        if (view != null)
            view.setVisibility(view.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
    }

    public static ArrayList<View> findAllChildren(View parent) {
        ArrayList<View> children = new ArrayList<View>();
        if (parent instanceof ViewGroup) {
            ViewGroup parentGroup = (ViewGroup) parent;
            int directChildren = parentGroup.getChildCount();
            for (int i = 0; i < directChildren; ++i) {
                View child = parentGroup.getChildAt(i);
                children.add(child);
                children.addAll(findAllChildren(child));
            }
        }
        return children;
    }

    public static ArrayList<View> findAllChildrenLeaves(View parent) {
        ArrayList<View> leaves = new ArrayList<View>();
        if (parent instanceof ViewGroup) {
            ViewGroup parentGroup = (ViewGroup) parent;
            int directChildren = parentGroup.getChildCount();
            for (int i = 0; i < directChildren; ++i) {
                View child = parentGroup.getChildAt(i);
                ArrayList<View> allChildrenLeaves = findAllChildrenLeaves(child);
                if (allChildrenLeaves.size() > 0)
                    leaves.addAll(allChildrenLeaves);
                else
                    leaves.add(child);
            }
        }
        return leaves;
    }

    /* The next three functions are here for lack of a better placement */

    private static Point sCachedWindowSize;
    public static Point getWindowSize(Activity activity) {
        if (sCachedWindowSize == null) {
            sCachedWindowSize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(sCachedWindowSize);
        }
        return sCachedWindowSize;
    }

    public static int getWindowWidth(Activity activity) {
        return getWindowSize(activity).x;
    }

    public static int getWindowHeight(Activity activity) {
        return getWindowSize(activity).y;
    }

}
