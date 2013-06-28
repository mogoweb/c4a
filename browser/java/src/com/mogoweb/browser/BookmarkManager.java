/*
 *  Copyright (c) 2012-2013, The Linux Foundation. All rights reserved.
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

package com.mogoweb.browser;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Class that manages bookmarks (add/remove/view bookmarks from database).
 */
public class BookmarkManager extends SQLiteOpenHelper {

    private static BookmarkManager sBookmarkManager;

    public static final String TABLE_BOOKMARKS = "bookmarks";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_URL = "url";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "bookmarks.db";
    private SQLiteDatabase mDatabase;

    private List<Bookmark> mBookmarks = new LinkedList<Bookmark>();

    private static final String DATABASE_CREATE = "create table "
             + TABLE_BOOKMARKS + "(" + COLUMN_ID
             + " integer primary key autoincrement, " + COLUMN_TITLE
             + " text not null , " + COLUMN_URL + " text not null, "
             + "UNIQUE ( "+ COLUMN_URL + "));";

    private final String[] allColumns = { COLUMN_ID, COLUMN_TITLE, COLUMN_URL };

    private BookmarkManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDatabase = this.getWritableDatabase();

        //read into memory
        readAllBookmarks();

        //prefill the database with the following if empty
        if (mBookmarks.size() == 0) {
            addBookmark("Google", "http://www.google.com/");
            addBookmark("Facebook", "http://www.facebook.com/");
            addBookmark("ebay", "http://www.ebay.com/");
            addBookmark("Amazon", "http://www.amazon.com/");
            addBookmark("CNN", "http://www.cnn.com/");
            addBookmark("NYTimes", "http://www.nytimes.com/");
            addBookmark("YouTube", "http://www.youtube.com/");
            addBookmark("SunSpider", "http://www.webkit.org/perf/sunspider-0.9.1/sunspider-0.9.1/driver.html");
        }
    }

        // Should be called only once
    public static BookmarkManager create(Context context) {
        if (sBookmarkManager == null) {
            sBookmarkManager = new BookmarkManager(context);
        }
        return sBookmarkManager;
    }

    public static BookmarkManager getInstance() {
        return sBookmarkManager;
    }

    public List<Bookmark> getAllBookmarks() {
        return mBookmarks;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
            int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
    }

    public boolean addBookmark(String title, String url) {
        if (title == null || url == null)
            return false;

        Bookmark bookmark = getBookmark(url);

        // Check already existing bookmark
        if (bookmark == null)
            createBookmarkEntry(title, url);
        else
            updateBookmarkEntry(bookmark, title, url);

        return true;
    }

    public Bookmark createBookmarkEntry(String title, String url) {
        Bookmark b = getBookmark(url);

        if (b == null) {  //no such bookamrk
            String fixedUrl = fixUrl(url);
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_URL, fixedUrl);

            long insertId = mDatabase.insert(TABLE_BOOKMARKS, null, values);
            if (insertId >= 0) {
                b = new Bookmark(insertId, title, fixedUrl);
                mBookmarks.add(b);
            }
        }

        return b;
    }

    public void deleteBookmark(Bookmark bookmark) {
        long id = bookmark.getId();
        mDatabase.delete(TABLE_BOOKMARKS, COLUMN_ID + " = " + id, null);
        mBookmarks.remove(bookmark);
    }

    public void updateBookmarkEntry(Bookmark bookmark, String title, String url) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_URL, fixUrl(url));
        String where = COLUMN_ID + "=" + bookmark.getId();
        mDatabase.update(TABLE_BOOKMARKS, values, where, null );
        bookmark.setTitle(title);
    }

    private void readAllBookmarks() {
        Cursor c = mDatabase.query(TABLE_BOOKMARKS, allColumns, null,
                null, null, null, null);
        //cursor is set to a row before the first entry..
        while (c.moveToNext()) {
            mBookmarks.add(new Bookmark(c.getLong(0), c.getString(1), c.getString(2)));
        }

        c.close();
    }

    public Bookmark getBookmark(String url) {
        String fixedUrl = fixUrl(url);

        for (Bookmark b: mBookmarks) {
            if (fixedUrl.equals(b.getUrl()))
                return b;
        }

        return null;
    }

    // This function ensures that the URL that is stored & checked
    // always is lower case and in proper format
    private static String fixUrl(String inUrl) {
        int colon = inUrl.indexOf(':');
        boolean allLower = true;
        for (int index = 0; index < colon; index++) {
            char ch = inUrl.charAt(index);
            if (!Character.isLetter(ch)) {
                break;
            }
            allLower &= Character.isLowerCase(ch);
            if (index == colon - 1 && !allLower) {
                inUrl = inUrl.substring(0, colon).toLowerCase(Locale.getDefault())
                        + inUrl.substring(colon);
            }
        }
        if (inUrl.startsWith("http://") || inUrl.startsWith("https://"))
            return inUrl;
        if (inUrl.startsWith("http:") ||
                inUrl.startsWith("https:")) {
            if (inUrl.startsWith("http:/") || inUrl.startsWith("https:/")) {
                inUrl = inUrl.replaceFirst("/", "//");
            } else inUrl = inUrl.replaceFirst(":", "://");
        }
        return inUrl;
    }

}
