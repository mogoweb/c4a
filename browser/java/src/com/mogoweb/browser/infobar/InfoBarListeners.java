// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.mogoweb.browser.infobar;

public class InfoBarListeners {

    public interface Dismiss {
        public void onInfoBarDismissed(InfoBar infoBar);
    }
}
