// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Copyright (c) 2012, The Linux Foundation. All rights reserved.
//
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.mogoweb.browser.web;

import com.mogoweb.browser.utils.Logger;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

class WebTabLayout extends FrameLayout {

    public WebTabLayout(Context context) {
        super(context);
        Logger.warn("new TabLayout");
    }

    public WebTabLayout(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        Logger.warn("new TabLayout");
    }
}

