package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

public class ChannelHeaderViewStyle extends BaseStyle {
    private int channelTitleTextSize;
    private int lastActiveTextSize;
    // Color
    private int channelTitleTextColor;
    private int lastActiveTextColor;
    // Style
    private int channelTitleTextStyle;
    private int lastActiveTextStyle;


    private boolean backButtonShow;
    private boolean lastActiveShow;

    private boolean avatarGroupShow;
    private Drawable backButtonBackground;

    private boolean optionsButtonShow;
    private Drawable optionsButtonBackground;
    private int optionsButtonTextSize;
    private int optionsButtonWidth;
    private int optionsButtonHeight;
    private boolean activeBadgeShow;


    public ChannelHeaderViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        setContext(c);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.ChannelHeaderView, 0, 0);

        // Channel Title
        channelTitleTextSize = (int) a.getDimension(R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextSize, getDimension(R.dimen.stream_channel_header_initials));
        channelTitleTextColor = a.getColor(R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextColor, getColor(R.color.stream_channel_initials));
        channelTitleTextStyle = a.getInt(R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextStyle, Typeface.BOLD);


        // Last Active
        lastActiveTextSize = (int) a.getDimension(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextSize, getDimension(R.dimen.stream_channel_preview_date));
        lastActiveTextColor = a.getColor(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextColor, getColor(R.color.gray_dark));
        lastActiveTextStyle = a.getInt(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextStyle, Typeface.NORMAL);

        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamAvatarWidth, getDimension(R.dimen.stream_channel_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamAvatarHeight, getDimension(R.dimen.stream_channel_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.ChannelHeaderView_streamAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.ChannelHeaderView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialTextSize = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials));
        avatarInitialTextColor = a.getColor(R.styleable.ChannelHeaderView_streamAvatarTextColor, Color.WHITE);
        avatarInitialTextStyle = a.getInt(R.styleable.ChannelHeaderView_streamAvatarTextStyle, Typeface.BOLD);

        lastActiveShow = a.getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveShow, true);

        // Back Button
        backButtonShow = a.getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderBackButtonShow, false);
        backButtonBackground = a.getDrawable(R.styleable.ChannelHeaderView_streamChannelHeaderBackButtonBackground);

        // Avatar

        avatarGroupShow = a.getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderAvatarShow, true);

        // Badge
        activeBadgeShow = a.getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderActiveBadgeShow, true);

        // Options
        optionsButtonShow = a.getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonShow, false);
        optionsButtonBackground = a.getDrawable(R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonBackground);
        optionsButtonTextSize = (int) a.getDimension(R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonTextSize, getDimension(R.dimen.stream_channel_header_initials));
        optionsButtonWidth = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonWidth, getDimension(R.dimen.stream_channel_avatar_width));
        optionsButtonHeight = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamChannelHeaderOptionsButtonHeight, getDimension(R.dimen.stream_channel_avatar_height));

        a.recycle();
    }

    public int getChannelTitleTextSize() {
        return channelTitleTextSize;
    }

    public int getChannelTitleTextColor() {
        return channelTitleTextColor;
    }

    public int getChannelTitleTextStyle() {
        return channelTitleTextStyle;
    }

    public int getLastActiveTextSize() {
        return lastActiveTextSize;
    }

    public int getLastActiveTextColor() {
        return lastActiveTextColor;
    }

    public int getLastActiveTextStyle() {
        return lastActiveTextStyle;
    }

    public boolean isBackButtonShow() {
        return backButtonShow;
    }

    public Drawable getBackButtonBackground() {
        return backButtonBackground != null ? backButtonBackground : getDrawable(R.drawable.arrow_left);
    }

    public boolean isLastActiveShow() {
        return lastActiveShow;
    }

    public boolean isAvatarGroupShow() {
        return avatarGroupShow;
    }

    public boolean isOptionsButtonShow() {
        return optionsButtonShow;
    }

    public Drawable getOptionsButtonBackground() {
        return optionsButtonBackground != null ? optionsButtonBackground : getDrawable(R.drawable.settings);
    }

    public boolean isActiveBadgeShow() {
        return activeBadgeShow;
    }

    public int getOptionsButtonTextSize() {
        return optionsButtonTextSize;
    }

    public int getOptionsButtonWidth() {
        return optionsButtonWidth;
    }

    public int getOptionsButtonHeight() {
        return optionsButtonHeight;
    }

}
