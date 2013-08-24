// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.mogoweb.browser;

public class MediaType {

    // the following values must sync enum MediaType in
    // src/third_party/WebKit/Source/WebKit/chromium/public/WebContextMenuData.h
    // No special node is in context.
    public static final int MEDIA_TYPE_NONE = 0;
    // An image node is selected.
    public static final int MEDIA_TYPE_IMAGE = 1;
    // A video node is selected.
    public static final int MEDIA_TYPE_VIDEO = 2;
    // An audio node is selected.
    public static final int MEDIA_TYPE_AUDIO = 3;
    // A file node is selected.
    public static final int MEDIA_TYPE_FILE = 4;
    // A plugin node is selected.
    public static final int MEDIA_TYPE_PLUGIN = 5;
}
