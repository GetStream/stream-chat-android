package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.LayoutRes;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

public class ChannelListViewStyle extends BaseStyle {
    final String TAG = ChannelListViewStyle.class.getSimpleName();
    // dimensions

    private int dateTextSize;
    private int titleTextSize;
    private int messageTextSize;
    // colors
    private int titleTextColor;
    private int unreadTitleTextColor;
    private int messageTextColor;
    private int unreadMessageTextColor;
    private int dateTextColor;
    // styles
    private int titleTextStyle;
    private int unreadTitleTextStyle;
    private int messageTextStyle;
    private int unreadMessageTextStyle;

    // layouts
    public @LayoutRes
    int channelPreviewLayout;


    public ChannelListViewStyle(Context c, AttributeSet attrs) {
        this.setContext(c);
        TypedArray a = c.obtainStyledAttributes(attrs,
                R.styleable.ChannelListView, 0, 0);

        avatarWidth = a.getDimension(R.styleable.ChannelListView_avatarWidth, c.getResources().getDimension(R.dimen.stream_channel_avatar_height));
        avatarHeight = a.getDimension(R.styleable.ChannelListView_avatarHeight, c.getResources().getDimension(R.dimen.stream_channel_avatar_width));
        initialsTextSize = a.getDimension(R.styleable.ChannelListView_initialsTextSize, c.getResources().getDimension(R.dimen.stream_channel_initials));
        initialsTextColor = a.getColor(R.styleable.ChannelListView_initialsTextColor, c.getResources().getColor(R.color.stream_channel_initials));
        initialsTextStyle = a.getInt(R.styleable.ChannelListView_initialsTextStyle, Typeface.NORMAL);

        dateTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_dateTextSize, -1);
        titleTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_titleTextSize, -1);
        messageTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_messageTextSize, -1);

        titleTextColor = a.getColor(R.styleable.ChannelListView_titleTextColor, this.getColor(R.color.black));
        unreadTitleTextColor = a.getColor(R.styleable.ChannelListView_unreadTitleTextColor, this.getColor(R.color.black));
        messageTextColor = a.getColor(R.styleable.ChannelListView_titleTextColor, this.getColor(R.color.gray_dark));
        unreadMessageTextColor = a.getColor(R.styleable.ChannelListView_titleTextColor, this.getColor(R.color.black));
        dateTextColor = a.getColor(R.styleable.ChannelListView_dateTextColor, -1);

        titleTextStyle = a.getInt(R.styleable.ChannelListView_titleTextStyleChannel, Typeface.BOLD);
        unreadTitleTextStyle = a.getInt(R.styleable.ChannelListView_unreadTitleTextStyle, Typeface.BOLD);
        messageTextStyle = a.getInt(R.styleable.ChannelListView_messageTextStyle, Typeface.NORMAL);
        unreadMessageTextStyle = a.getInt(R.styleable.ChannelListView_unreadMessageTextStyle, Typeface.BOLD);

        channelPreviewLayout = a.getResourceId(R.styleable.ChannelListView_channelPreviewLayout, R.layout.list_item_channel);

        a.recycle();
    }

    public int getDateTextSize() {
        return dateTextSize;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public int getMessageTextSize() {
        return messageTextSize;
    }

    public int getTitleTextColor() {
        return titleTextColor;
    }

    public int getUnreadTitleTextColor() {
        return unreadTitleTextColor;
    }

    public int getMessageTextColor() {
        return messageTextColor;
    }

    public int getUnreadMessageTextColor() {
        return unreadMessageTextColor;
    }

    public int getDateTextColor() {
        return dateTextColor;
    }

    public int getTitleTextStyle() {
        return titleTextStyle;
    }

    public int getUnreadTitleTextStyle() {
        return unreadTitleTextStyle;
    }

    public int getMessageTextStyle() {
        return messageTextStyle;
    }

    public int getUnreadMessageTextStyle() {
        return unreadMessageTextStyle;
    }
}
