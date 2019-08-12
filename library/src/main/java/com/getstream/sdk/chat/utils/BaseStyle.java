package com.getstream.sdk.chat.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

public class BaseStyle {
    private Context context;



    public Drawable getDrawable(@DrawableRes int drawable) {
        return ContextCompat.getDrawable(getContext(), drawable);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getColor(@ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }
}
