package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntegerRes;
import androidx.core.content.ContextCompat;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.style.TextStyle;

public class BaseStyle {

    protected Context context;
    protected Resources resources;

    protected int avatarWidth;
    protected int avatarHeight;

    protected int avatarBorderWidth;
    protected int avatarBorderColor;
    protected int avatarBackGroundColor;

    protected TextStyle avatarInitialText;
    protected TextStyle readStateText;

    protected boolean showReadState;
    protected int readStateAvatarWidth;
    protected int readStateAvatarHeight;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        this.resources = context.getResources();
    }

    protected final int getSystemAccentColor() {
        return getSystemColor(R.attr.colorAccent);
    }

    protected final int getSystemPrimaryColor() {
        return getSystemColor(R.attr.colorPrimary);
    }

    protected final int getSystemPrimaryDarkColor() {
        return getSystemColor(R.attr.colorPrimaryDark);
    }

    protected final int getSystemPrimaryTextColor() {
        return getSystemColor(android.R.attr.textColorPrimary);
    }

    protected final int getSystemHintColor() {
        return getSystemColor(android.R.attr.textColorHint);
    }

    protected final int getSystemColor(@AttrRes int attr) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, 0);
        a.recycle();

        return color;
    }

    protected final int getDimension(@DimenRes int dimen) {
        return resources.getDimensionPixelSize(dimen);
    }

    protected final int getColor(@ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    protected final int getInteger(@IntegerRes int intRes) {
        return context.getResources().getInteger(intRes);
    }

    protected final Drawable getDrawable(@DrawableRes int drawable) {
        if (drawable == -1) return null;
        return ContextCompat.getDrawable(context, drawable);
    }

    protected final Drawable getVectorDrawable(@DrawableRes int drawable) {
        return ContextCompat.getDrawable(context, drawable);
    }

    public int getAvatarWidth() {
        return avatarWidth;
    }

    public int getAvatarHeight() {
        return avatarHeight;
    }


    public int getAvatarBorderWidth() {
        return avatarBorderWidth;
    }

    public int getAvatarBorderColor() {
        return avatarBorderColor;
    }

    public int getAvatarBackGroundColor() {
        return avatarBackGroundColor;
    }

    public boolean isReadStateEnabled() {
        return showReadState;
    }

    public int getReadStateAvatarWidth() {
        return readStateAvatarWidth;
    }

    public int getReadStateAvatarHeight() {
        return readStateAvatarHeight;
    }
}
