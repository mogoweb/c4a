package com.mogoweb.browser.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageButton;

public class NumberView extends ImageButton {
    private String mNumber = "1";

    Paint mPaint;

    public NumberView(Context context) {
        super(context);
    }

    public NumberView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(24);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setNumber(String s) {
        mNumber = s;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = getWidth() / 2;
        int y = getHeight() * 6 / 10;

        canvas.drawText(mNumber, x, y, mPaint);
    }
}
