package com.getstream.sdk.chat.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

public class BaseStyle {
    private Context context;

    protected float avatarWidth;
    protected float avatarHeight;

    protected float avatarBorderWidth;
    protected int avatarBorderColor;
    protected int avatarBackGroundColor;

    protected float avatarInitialTextSize;
    protected int avatarInitialTextColor;
    protected int avatarInitialTextStyle;

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

    public float getAvatarWidth() {
        return avatarWidth;
    }

    public float getAvatarHeight() {
        return avatarHeight;
    }

    public float getAvatarInitialTextSize() {
        return avatarInitialTextSize;
    }

    public int getAvatarInitialTextColor() {
        return avatarInitialTextColor;
    }

    public int getAvatarInitialTextStyle() {
        return avatarInitialTextStyle;
    }

    public float getAvatarBorderWidth() {
        return avatarBorderWidth;
    }

    public int getAvatarBorderColor() {
        return avatarBorderColor;
    }

    public int getAvatarBackGroundColor() {
        return avatarBackGroundColor;
    }
}
