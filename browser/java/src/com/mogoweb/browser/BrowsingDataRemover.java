package com.mogoweb.browser;

import com.mogoweb.browser.utils.Logger;

public class BrowsingDataRemover {

    private static final int CLEAR_HISTORY = 0;
    private static final int CLEAR_CACHE = 1;
    private static final int CLEAR_COOKIES_AND_SITE_DATA = 2;
    private static final int CLEAR_PASSWORDS = 3;
    private static final int CLEAR_FORM_DATA = 4;

    public static void clearData(int type) {
        switch (type) {
        case CLEAR_HISTORY:
            nativeClearHistory();
            break;
        case CLEAR_CACHE:
            nativeClearCache();
            break;
        case CLEAR_COOKIES_AND_SITE_DATA:
            nativeClearCookies();
            break;
        case CLEAR_PASSWORDS:
            nativeClearPasswords();
            break;
        case CLEAR_FORM_DATA:
            nativeClearFormData();
            break;
        default:
            Logger.warn("unkown clear data type: " + type);
            break;
        }
    }

    private static native void nativeClearHistory();
    private static native void nativeClearCache();
    private static native void nativeClearCookies();
    private static native void nativeClearPasswords();
    private static native void nativeClearFormData();
}
