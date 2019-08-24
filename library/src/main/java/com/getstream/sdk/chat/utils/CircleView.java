package com.getstream.sdk.chat.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.getstream.sdk.chat.R;

public class CircleView extends View {
    private Context context;
    public CircleView(Context context) {
        super(context);
        this.context = context;
    }
    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }
    public CircleView(Context context, AttributeSet attrs, Integer defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        Paint paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.stream_user_intials_background));
        paint.setStyle(Paint.Style.FILL);
        float radius = getWidth()/2f;
        canvas.drawCircle(getWidth()/2f, getWidth()/2f, radius, paint);
    }
}
