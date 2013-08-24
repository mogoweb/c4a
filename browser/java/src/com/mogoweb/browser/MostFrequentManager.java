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

import com.mogoweb.browser.TabManager.TabData;
import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.views.TileView;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that manages mostfrequent (add/remove/view mostfrequent from database).
 */
public class MostFrequentManager extends SQLiteOpenHelper {
    public static final String TAG = "SWE_UI";
    public static final String TABLE_MOST_FREQUENT = "mostfrequent";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_COUNTER = "counter";
    public static final String COLUMN_IMAGE = "image";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "mostfrequent.db";
    private static final int MOST_FREQUENT_LIMIT = 10;
    private static int mWidth;

    private static final String mQuery = "create table " + TABLE_MOST_FREQUENT + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TITLE + " text, " + COLUMN_URL + " text not null, "
            + COLUMN_COUNTER + " integer, " + COLUMN_IMAGE + " blob, " + "UNIQUE ( " + COLUMN_URL + "));"; // "

    private final String[] allColumns = { COLUMN_ID, COLUMN_TITLE, COLUMN_URL, COLUMN_COUNTER, COLUMN_IMAGE };
    private static MostFrequentManager sMostFrequentManager;
    private SQLiteDatabase mDatabase;


    public static MostFrequentManager getInstance() {
        return sMostFrequentManager;
    }

    private MostFrequentManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sMostFrequentManager = this;
        // We use a smaller size for favorite icons (2/3 of the normal size)
        mWidth = 2 * context.getResources().getDimensionPixelSize(R.dimen.TilesSize) / 3;
        // and we subtract the 2 margins of the TileView
        mWidth += -2 * TileView.SHADOW_MARGIN_PX;

        mDatabase = getWritableDatabase();
        startListening(TabManager.getInstance());
    }

    // Should be called only once
    public static MostFrequentManager create(Context context) {
        if (sMostFrequentManager == null) {
            sMostFrequentManager = new MostFrequentManager(context);
        }
        return sMostFrequentManager;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(TAG, "mQuery: " + mQuery);
        database.execSQL(mQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_MOST_FREQUENT);
    }

    public void startListening(TabManager tm) {
        tm.addListener( new TabManager.Listener() {
            MyTabListener li = new MyTabListener();

            public void onTabSelected(TabData td, boolean bSelected) {
                if (bSelected) {
                    li.setTab(td);
                    td.tab.addListener(li);
                } else {
                    td.tab.removeListener(li);
                    li.setTab(null);
                }
            }

            public void onTabAdded(TabData td, int location) {}
            public void onTabRemoved(TabData td, int location) {}
            public void onTabShow(TabData td) {}
        });
    }

    public void onNewMostFrequentEntry(String title, String url, Bitmap image) {

        if (image == null)
            return;

        // Preferred way to query in SQLite database
        String query = "select * from " + TABLE_MOST_FREQUENT + " where " + COLUMN_URL + " = ?";
        Cursor cursor = mDatabase.rawQuery(query, new String[] { url });

        // cursor check if entry exists
        if (cursor.moveToFirst()) {
            Log.v(TAG, "Url " + url + " already exists in database");
            int id      = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            int counter = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNTER));

            ContentValues values = new ContentValues();
            values.put(COLUMN_COUNTER, ++counter);

            mDatabase.update(TABLE_MOST_FREQUENT, values, COLUMN_ID + "=" + id, null);
        } else {
            Log.v(TAG, "adding " + url + "to the mf database");
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_URL, url);
            values.put(COLUMN_COUNTER, 1);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();
            values.put(COLUMN_IMAGE, bytes);

            mDatabase.insert(TABLE_MOST_FREQUENT, null, values);
        }

        cursor.close();
    }

    public List<MostFrequent> getAllMostFrequents() {
        List<MostFrequent> sites = new ArrayList<MostFrequent>();

        Cursor cursor = mDatabase.query(TABLE_MOST_FREQUENT, allColumns, null, null, null, null, COLUMN_COUNTER + " DESC", "" + MOST_FREQUENT_LIMIT);

        while (cursor.moveToNext()) {
            MostFrequent mf = new MostFrequent();
            mf.title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
            mf.url = cursor.getString(cursor.getColumnIndex(COLUMN_URL));
            mf.counter = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNTER));
            byte[] bytes = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
            mf.image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            sites.add(mf);
        }

        cursor.close();
        return sites;
    }

    public static class MostFrequent {
        public long id;
        public String title;
        public String url;
        public int counter;
        public Bitmap image;
    }

    private class MyTabListener implements Tab.Listener {
        TabData mTabData;

        public void setTab(TabData td) {
            mTabData = td;
        }

        @Override
        public void onLoadProgressChanged(int progress) {}

        public void onLoadStarted(boolean isMainFrame) {}
        public void onLoadStopped(boolean isMainFrame) {
            // Only when the main frame is loaded
            if (isMainFrame) {
                Tab tab = mTabData.tab;
                String url = tab.getUrl();
                if (url.indexOf(TabManager.DEFAULT_SEARCH_QUERY_PREFIX) == -1) {
                    getInstance().onNewMostFrequentEntry(tab.getTitle(), url,
                                        tab.getSnapshot(mWidth, mWidth));
                }
            }
        }
        public void onUpdateUrl(String url) {}
        public void showContextMenu(String url, int mediaType, String linkText,
                String unfilteredLinkUrl, String srcUrl) {}
        public void didFailLoad(boolean isProvisionalLoad, boolean isMainFrame,
                    int errorCode, String description, String failingUrl) {}
        @Override
        public void didStartLoading(String url) {
        }

        @Override
        public void didStopLoading(String url) {
        }
    }
}
