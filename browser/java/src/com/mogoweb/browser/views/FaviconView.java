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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class FaviconView extends TextView {

    private Drawable mFavDrawable;

    public FaviconView(Context context) {
        super(context);
        initializeView();
    }

    public FaviconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaviconView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView();
    }

    public void setFavDrawable(Drawable favDrawable) {
        mFavDrawable = favDrawable;
        regenerateAutoFavicon();
    }

    private void initializeView() {
        // setText will be called anyway, even with empty text, so we skip this
        //regenerateAutoFavicon();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        regenerateAutoFavicon();
    }

    private void regenerateAutoFavicon() {
        Drawable favDrawable = mFavDrawable;

        // if the FavDrawable is not set, then use the automatic one
        if (favDrawable == null) {

            Bitmap favicon = DesignShared.getEmptyFavicon(getResources());
            if (favicon != null) {

                // handle no text or editing (just bitmap)
                CharSequence charSequence = getText();
                if (charSequence != null && charSequence.length() > 0 && !isInEditMode()) {

                    // make the bitmap mutable
                    favicon = favicon.copy(favicon.getConfig(), true);

                    String lowerCaseInitial = Character.toString(Character.toLowerCase(charSequence.charAt(0)));

                    // draw the letter
                    if (favicon != null) {
                        final int fW = favicon.getWidth();
                        final int fH = favicon.getHeight();
                        final int tW = (int) DesignShared.FaviconTextPaint.measureText(lowerCaseInitial);
                        final int tH = (int) (DesignShared.FaviconTextSizeDp * DesignShared.DP_TO_PX);
                        final int mX = (int) (-3 * DesignShared.DP_TO_PX);
                        final int mY = (int) (0 * DesignShared.DP_TO_PX);
                        Canvas painter = new Canvas(favicon);
                        painter.drawText(lowerCaseInitial, (fW - tW) / 2 + mX, (fH + 2 * tH / 3) / 2 + mY, DesignShared.FaviconTextPaint);
                    }

                }

                // make the drawable out of the favicon
                favDrawable = new BitmapDrawable(getResources(), favicon);

            }

        }

        // set the processed bitmap to the left
        setCompoundDrawablesWithIntrinsicBounds(favDrawable, null, null, null);
    }

}
