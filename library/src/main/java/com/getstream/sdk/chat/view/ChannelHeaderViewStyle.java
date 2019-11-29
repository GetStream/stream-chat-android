package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.style.TextStyle;

public class ChannelHeaderViewStyle extends BaseStyle {

    public TextStyle channelTitleText;
    public TextStyle lastActiveText;

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
    private String offlineText;
    private String channelWithoutNameText;

    public ChannelHeaderViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        setContext(c);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.ChannelHeaderView, 0, 0);


        channelWithoutNameText = a.getString(R.styleable.ChannelHeaderView_streamChannelWithOutNameTitleText);

        channelTitleText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextSize, getDimension(R.dimen.stream_channel_header_initials))
                .color(R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextColor, getColor(R.color.stream_channel_initials))
                .style(R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextStyle, Typeface.BOLD)
                .font(R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextFontAssets, R.styleable.ChannelHeaderView_streamChannelHeaderTitleTextFont)
                .build();

        lastActiveText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextSize, getDimension(R.dimen.stream_channel_preview_date))
                .color(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextColor, getColor(R.color.stream_gray_dark))
                .font(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextFontAssets, R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextFont)
                .style(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveTextStyle, Typeface.NORMAL)
                .build();

        offlineText = a.getString(R.styleable.ChannelHeaderView_streamChannelHeaderOfflineText);

        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamAvatarWidth, getDimension(R.dimen.stream_channel_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamAvatarHeight, getDimension(R.dimen.stream_channel_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.ChannelHeaderView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.ChannelHeaderView_streamAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.ChannelHeaderView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelHeaderView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials))
                .color(R.styleable.ChannelHeaderView_streamAvatarTextColor, Color.WHITE)
                .font(R.styleable.ChannelHeaderView_streamAvatarTextFontAssets, R.styleable.ChannelHeaderView_streamAvatarTextFont)
                .style(R.styleable.ChannelHeaderView_streamAvatarTextStyle, Typeface.BOLD)
                .build();

        lastActiveShow = a.getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderLastActiveShow, true);

        // Back Button
        backButtonShow = a.getBoolean(R.styleable.ChannelHeaderView_streamChannelHeaderBackButtonShow, true);
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

    public String getChannelWithoutNameText() {
        return !TextUtils.isEmpty(channelWithoutNameText) ? channelWithoutNameText : context.getString(R.string.stream_channel_unknown_title);
    }

    public boolean isBackButtonShow() {
        return backButtonShow;
    }

    public Drawable getBackButtonBackground() {
        return backButtonBackground != null ? backButtonBackground : getDrawable(R.drawable.stream_arrow_left);
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
        return optionsButtonBackground != null ? optionsButtonBackground : getDrawable(R.drawable.stream_ic_settings);
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

    public String getOfflineText() {
        return offlineText != null ? offlineText : context.getString(R.string.stream_channel_offlineText);
    }
}
