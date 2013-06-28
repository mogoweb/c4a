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

import com.mogoweb.browser.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public class DesignShared {

    public static float DP_TO_PX;

    public static Paint WhitePaint;
    public static Paint SDRedPaint;

    public static Paint TileTextBackgroundPaint;
    public static Paint TileTextPaint;
    public static final int TileTitleTextSizeDp = 12;

    public static Paint FaviconTextPaint;
    public static final int FaviconTextSizeDp = 18;

    public static void initialize(Resources res) {
        DP_TO_PX = res.getDisplayMetrics().density;
        WhitePaint = new Paint();
        WhitePaint.setColor(Color.WHITE);
        SDRedPaint = new Paint();
        SDRedPaint.setColor(res.getColor(R.color.SDRed));
        TileTextBackgroundPaint = new Paint();
        TileTextBackgroundPaint.setColor(res.getColor(R.color.HomeTileTitleBackground));
        TileTextPaint = new Paint();
        TileTextPaint.setAntiAlias(true);
        TileTextPaint.setColor(Color.WHITE);
        TileTextPaint.setTypeface(Typeface.create("sans-serif" /* sans-serif-condensed ? */, Typeface.NORMAL));
        TileTextPaint.setTextSize(TileTitleTextSizeDp * DP_TO_PX);
        FaviconTextPaint = new Paint();
        FaviconTextPaint.setAntiAlias(true);
        FaviconTextPaint.setColor(Color.BLACK);
        FaviconTextPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        FaviconTextPaint.setTextSize(FaviconTextSizeDp * DP_TO_PX);
    }

    public static Drawable getLightShadowDrawable(Resources res) {
        if (sShadowLight == null)
            sShadowLight = res.getDrawable(R.drawable.cosmetic_shadow_outlined_4);
        return sShadowLight;
    }

    public static Bitmap getTileCloseOverlay(Resources res, boolean pressed) {
        if (sTileCloseOverlay == null)
            sTileCloseOverlay = BitmapFactory.decodeResource(res, R.drawable.tile_close_corner);
        if (sTileCloseOverlayPressed == null)
            sTileCloseOverlayPressed = BitmapFactory.decodeResource(res, R.drawable.tile_close_corner_on);
        return pressed ? sTileCloseOverlayPressed : sTileCloseOverlay;
    }

    public static Point getTileCloseOverlaySize(Resources res) {
        Bitmap bmp = getTileCloseOverlay(res, false);
        return new Point(bmp.getWidth(), bmp.getHeight());
    }

    public static Bitmap getEmptyFavicon(Resources res) {
        if (sEmptyFavicon == null)
            sEmptyFavicon = BitmapFactory.decodeResource(res, R.drawable.fav_empty);
        return sEmptyFavicon;
    }

    public static Bitmap getEmptyTabBitmap(Resources res) {
        if (sEmptyTabBitmap == null)
            sEmptyTabBitmap = BitmapFactory.decodeResource(res, R.drawable.tile_empty_120);
        return sEmptyTabBitmap;
    }

    private static Drawable sShadowLight;
    private static Bitmap sTileCloseOverlay;
    private static Bitmap sTileCloseOverlayPressed;
    private static Bitmap sEmptyFavicon;
    private static Bitmap sEmptyTabBitmap;

}
