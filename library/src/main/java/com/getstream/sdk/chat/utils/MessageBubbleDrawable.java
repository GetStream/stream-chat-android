package com.getstream.sdk.chat.utils;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class MessageBubbleDrawable extends Drawable {

    private Paint backgroundPaint;
    private Paint borderPaint;

    private int borderColor;
    private int borderWidth;
    private int backgroundColor;

    private float borderwWidthInPx;

    public MessageBubbleDrawable(int borderColor, int borderWidth, int backgroundColor) {

        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.borderwWidthInPx = dipToPixels(borderWidth);
        this.backgroundColor = backgroundColor;

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dipToPixels(borderWidth));
        borderPaint.setColor(borderColor);
    }

    public static float dipToPixels(float dipValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, Resources.getSystem().getDisplayMetrics());
    }

    static public Path RoundedRect(float left, float top, float right, float bottom, float rx, float ry, boolean conformToOriginalPost) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.arcTo(right - 2 * rx, top, right, top + 2 * ry, 0, -90, false); //top-right-corner
        path.rLineTo(-widthMinusCorners, 0);
        path.arcTo(left, top, left + 2 * rx, top + 2 * ry, 270, -90, false);//top-left corner.
        path.rLineTo(0, heightMinusCorners);
        if (conformToOriginalPost) {
            path.rLineTo(0, ry);
            path.rLineTo(width, 0);
            path.rLineTo(0, -ry);
        } else {
            path.arcTo(left, bottom - 2 * ry, left + 2 * rx, bottom, 180, -90, false); //bottom-left corner
            path.rLineTo(widthMinusCorners, 0);
            path.arcTo(right - 2 * rx, bottom - 2 * ry, right, bottom, 90, -90, false); //bottom-right corner
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.
        return path;
    }

    @Override
    public void draw(Canvas canvas) {
        int height = getBounds().height();
        int width = getBounds().width();

        // draw a square to fill halve
        //canvas.drawRect(new RectF(0.0f, 0.0f, width/2, height), backgroundPaint);
        // draw a rounded rectangle for nice border starting from 25%
        // canvas.drawRoundRect(new RectF(0.0f, 0.0f, width, height), dipToPixels(16), dipToPixels(16), backgroundPaint);

        // first draw the 2 borders
        //canvas.drawRect(rect2, borderPaint);
        //canvas.drawRoundRect(rect, dipToPixels(16), dipToPixels(16), borderPaint);

        // now fill it up with the background paint
        //rect = new RectF(borderwWidthInPx, borderwWidthInPx, width -borderwWidthInPx, height - borderwWidthInPx);
        //rect2 = new RectF(borderwWidthInPx, borderwWidthInPx, width/2, height- borderwWidthInPx);
        Path mypath = RoundedRect(0, 0, width, height, 30.0f, 30.0f, false);
        canvas.drawPath(mypath, borderPaint);


    }

    @Override
    public void setAlpha(int alpha) {
        backgroundPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        backgroundPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

}