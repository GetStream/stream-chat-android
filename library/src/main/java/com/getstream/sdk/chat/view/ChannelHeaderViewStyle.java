package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

public class ChannelHeaderViewStyle extends BaseStyle {
    private int channelTitleTextSize;
    // Color
    private int channelTitleTextColor;
    // Style
    private int channelTitleTextStyle;
    private boolean backButtonShow;

    public ChannelHeaderViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        this.setContext(c);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.ChannelHeaderView, 0, 0);

        channelTitleTextSize = (int) a.getDimension(R.styleable.ChannelHeaderView_channelTitleTextSize, c.getResources().getDimension(R.dimen.stream_channel_initials));
        channelTitleTextColor = a.getColor(R.styleable.ChannelHeaderView_channelTitleTextColor, c.getResources().getColor(R.color.stream_channel_initials));
        channelTitleTextStyle = a.getInt(R.styleable.ChannelHeaderView_channelTitleTextStyle, Typeface.NORMAL);

        avatarWidth = a.getDimension(R.styleable.ChannelHeaderView_channelAvatarWidth, c.getResources().getDimension(R.dimen.stream_channel_avatar_height));
        avatarHeight = a.getDimension(R.styleable.ChannelHeaderView_channelAvatarHeight, c.getResources().getDimension(R.dimen.stream_channel_avatar_width));

        initialsTextSize = a.getDimension(R.styleable.ChannelHeaderView_channelAvatarTextSize, c.getResources().getDimension(R.dimen.stream_channel_initials));
        initialsTextColor = a.getColor(R.styleable.ChannelHeaderView_channelAvatarTextColor, c.getResources().getColor(R.color.stream_channel_initials));
        initialsTextStyle = a.getInt(R.styleable.ChannelHeaderView_channelAvatarTextStyle, Typeface.NORMAL);

        backButtonShow = a.getBoolean(R.styleable.ChannelHeaderView_backButtonShow, false);
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

    public boolean isBackButtonShow() {
        return backButtonShow;
    }
}
