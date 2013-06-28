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


package com.mogoweb.browser.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class TileView extends ImageView implements View.OnClickListener, View.OnLongClickListener {

    private static final int ACTIVE_BAR_HEIGHT_PX = (int) (4 * DesignShared.DP_TO_PX);
    private static final int TITLE_BAR_HEIGHT_PX = (int) (24 * DesignShared.DP_TO_PX);
    private static final int MARGIN_PX = (int) (4 * DesignShared.DP_TO_PX);
    public static final int SHADOW_MARGIN_PX = MARGIN_PX;
    private static final boolean DEFAULT_HAS_SHADOW = true;

    private Listener mListener;
    private boolean mHasShadow = DEFAULT_HAS_SHADOW;
    private boolean mCloseable;
    private boolean mIsActive;
    private boolean mClosingPressed;
    private String mTitle;
    private Rect mClosingRect;
    private Rect mCurrentRect;
    private Rect mTitleRect;
    public enum TileType {
        TILE_TYPE_TAB,
        TILE_TYPE_MOST_FREQUENT
    };
    private TileType mType;
    private String mUrl;

    public interface Listener {

        /**
         * This gets called when the Tile view is tapped.
         */
        void onTileClicked(TileView tile);

        /**
         * This gets invoked if the tile is 'Closeable' and the close icon is tapped.
         */
        void onTileRequestClose(TileView tile);

        /**
        * This gets called when the Tile view is long clicked
        */
        boolean onTileLongClicked(TileView tile);

    }

    public TileView(Context context) {
        super(context);
        initTileView();
    }

    public TileView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTileView();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setHasShadow(boolean hasShadow) {
        if (mHasShadow != hasShadow) {
            mHasShadow = hasShadow;
            requestLayout();
            invalidate();
        }
    }

    public void setCloseable(boolean closeable) {
        if (closeable != mCloseable) {
            mCloseable = closeable;
            invalidate();
        }
    }

    public void setIsActive(boolean isActive) {
        if (mIsActive != isActive) {
            mIsActive = isActive;
            invalidate();
        }
    }

    public void setTitle(String title) {
        if (mTitle != null && title != null && mTitle.equals(title))
            return;
        mTitle = title;
        invalidate();
    }

    // FIXME: move to constructor
    public void setType(TileType type) {
        mType = type;
    }

    public TileType getType() {
        return mType;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    private void initTileView() {
        setClickable(true);
        setOnClickListener(this);
        setLongClickable(true);
        setOnLongClickListener(this);
        setScaleType(ScaleType.CENTER);
        mClosingRect = new Rect();
        mCurrentRect = new Rect();
        mTitleRect = new Rect();
    }

    public void ensureVisible() {
        post(new Runnable() {
                @Override
                public void run() {
                    requestRectangleOnScreen(new Rect(getLeft(), getTop(), getRight(), getBottom()));
                }
            });
    }

    @Override
    public void onClick(View thisView) {
        if (mListener != null)
            mListener.onTileClicked(this);
    }

    @Override
    public boolean onLongClick(View v) {
        if (mListener != null)
            return mListener.onTileLongClicked(this);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // handle the close button 'hot spot' separately from the
        if (mCloseable && !mClosingRect.isEmpty()) {
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            boolean insideClose = x >= mClosingRect.left && x <= mClosingRect.right && y >= mClosingRect.top && y <= mClosingRect.bottom;

            if (insideClose && action == MotionEvent.ACTION_DOWN) {

                mClosingPressed = true;
                invalidate();
                return true;

            } else if (mClosingPressed) {

                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {

                    // emit the click signal
                    if (action == MotionEvent.ACTION_UP)
                        if (mListener != null)
                            mListener.onTileRequestClose(this);

                    // reset the state of the hot spot
                    mClosingPressed = false;
                    invalidate();

                }

                return true;
            }

        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // measure the ImageView...
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int iW = getMeasuredWidth();
        int iH = getMeasuredHeight();

        // add space for shadow
        if (mHasShadow) {
            iW += 2 * MARGIN_PX;
            iH += 2 * MARGIN_PX;
        }

        // add space if current
        if (mIsActive) {
            // This was never checked...
            // iH += CURRENT_BAR_HEIGHT_PX;
        }

        setMeasuredDimension(iW, iH);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // recalculate the active areas
        Point clsSize = DesignShared.getTileCloseOverlaySize(getResources());
        mClosingRect.set(right - left - clsSize.x - MARGIN_PX, MARGIN_PX, right - left - MARGIN_PX, clsSize.y + MARGIN_PX);
        mCurrentRect.set(MARGIN_PX - 1, bottom - top - ACTIVE_BAR_HEIGHT_PX, right - left - MARGIN_PX + 1, bottom - top);
        mTitleRect.set(MARGIN_PX, bottom - top - ACTIVE_BAR_HEIGHT_PX - TITLE_BAR_HEIGHT_PX, right - left - MARGIN_PX, bottom - top - ACTIVE_BAR_HEIGHT_PX);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw the bottom shadow
        if (mHasShadow) {
            Drawable shadow = DesignShared.getLightShadowDrawable(getResources());
            shadow.setBounds(0, 0, getWidth(), getHeight());
            shadow.draw(canvas);
        }

        // draw the ImageView with the current style
        super.onDraw(canvas);

        // overdraw the 'current' indicator
        if (mIsActive && !mCurrentRect.isEmpty()) {
            canvas.drawRect(mCurrentRect, DesignShared.SDRedPaint);
        }

        // overdraw the 'title'
        if (mTitle != null && !mTitleRect.isEmpty()) {
            canvas.drawRect(mTitleRect, DesignShared.TileTextBackgroundPaint);

            // draw the text (clipped)
            canvas.save();
            canvas.clipRect(mTitleRect);

            canvas.drawText(mTitle, mTitleRect.left + 4 * DesignShared.DP_TO_PX,
                    (mTitleRect.top + mTitleRect.bottom + 11 * DesignShared.TileTitleTextSizeDp / 8) / 2,
                    DesignShared.TileTextPaint);
            canvas.restore();
        }

        // overdraw the 'closing' interactive icon
        if (mCloseable && !mClosingRect.isEmpty()) {
            canvas.drawBitmap(DesignShared.getTileCloseOverlay(getResources(), mClosingPressed),
                    mClosingRect.left, mClosingRect.top, DesignShared.WhitePaint);
        }
    }

}
