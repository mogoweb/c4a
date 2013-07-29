// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.mogoweb.browser.infobar;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class InfoBar implements OnClickListener {

    public static final int ACTION_TYPE_NONE = 0;
    public static final int ACTION_TYPE_OK = 1;
    public static final int ACTION_TYPE_CANCEL = 2;
    public static final int ACTION_TYPE_AUTOLOGIN = 3;
    public static final int ACTION_TYPE_TRANSLATE = 4;
    public static final int ACTION_TYPE_TRANSLATE_SHOW_ORIGINAL = 5;

    static final int BACKGROUND_TYPE_INFO = 0;
    static final int BACKGROUND_TYPE_WARNING = 1;

    private static final String TAG = "InfoBar";

    private static int sIdCounter = 0;

    private final int mBackgroundType = BACKGROUND_TYPE_INFO;

    private InfoBarContainer mContainer;

    private ContentWrapperView mContentView;

    private Context mContext;

    private boolean mControlsEnabled;

    private boolean mExpireOnNavigation;

    private final int mId = 0;

    private boolean mIsDismissed;

    private InfoBarListeners.Dismiss mListener;

    public void onClick(View v) {

    }
}
