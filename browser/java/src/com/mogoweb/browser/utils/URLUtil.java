// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.mogoweb.browser.utils;

public class URLUtil {

    /**
     * Guesses canonical filename that a download would have, using
     * the URL and contentDisposition. File extension, if not defined,
     * is added based on the mimetype
     * @param url Url to the content
     * @param contentDisposition Content-Disposition HTTP header or null
     * @param mimeType Mime-type of the content or null
     *
     * @return suggested filename
     */
    public static final String guessFileName(
            String url,
            String contentDisposition,
            String mimeType) {
        return nativeGetSuggestedFilename(url, contentDisposition, mimeType);
    }

    private static native String nativeGetSuggestedFilename(
            String url,
            String contentDisposition,
            String mimeType);
}
