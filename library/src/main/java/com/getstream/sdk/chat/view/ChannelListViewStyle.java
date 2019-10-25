package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;

import com.getstream.sdk.chat.R;

public class ChannelListViewStyle extends BaseStyle {
    final String TAG = ChannelListViewStyle.class.getSimpleName();
    // dimensions
    // layouts
    public @LayoutRes
    int channelPreviewLayout;
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
    private String channelWithoutNameText;

    public ChannelListViewStyle(Context c, AttributeSet attrs) {
        this.setContext(c);
        TypedArray a = c.obtainStyledAttributes(attrs,
                R.styleable.ChannelListView, 0, 0);

        channelWithoutNameText = a.getString(R.styleable.ChannelListView_streamChannelWithOutNameTitleText);
        titleTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamTitleTextSize, getDimension(R.dimen.stream_channel_item_title));
        messageTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamMessageTextSize, getDimension(R.dimen.stream_channel_item_message));
        dateTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamLastMessageDateTextSize, getDimension(R.dimen.stream_channel_item_message_date));

        titleTextColor = a.getColor(R.styleable.ChannelListView_streamTitleTextColor, getColor(R.color.stream_black));
        unreadTitleTextColor = a.getColor(R.styleable.ChannelListView_streamUnreadTitleTextColor, getColor(R.color.stream_black));
        messageTextColor = a.getColor(R.styleable.ChannelListView_streamMessageTextColor, getColor(R.color.stream_gray_dark));
        unreadMessageTextColor = a.getColor(R.styleable.ChannelListView_streamUnreadMessageTextColor, getColor(R.color.stream_black));
        dateTextColor = a.getColor(R.styleable.ChannelListView_streamLastMessageDateTextColor, getColor(R.color.stream_gray_dark));

        titleTextStyle = a.getInt(R.styleable.ChannelListView_streamTitleTextStyle, Typeface.BOLD);
        unreadTitleTextStyle = a.getInt(R.styleable.ChannelListView_streamUnreadTitleTextStyle, Typeface.BOLD);
        messageTextStyle = a.getInt(R.styleable.ChannelListView_streamMessageTextStyle, Typeface.NORMAL);
        unreadMessageTextStyle = a.getInt(R.styleable.ChannelListView_streamUnreadMessageTextStyle, Typeface.BOLD);

        channelPreviewLayout = a.getResourceId(R.styleable.ChannelListView_streamChannelPreviewLayout, R.layout.stream_item_channel);

        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarWidth, getDimension(R.dimen.stream_channel_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarHeight, getDimension(R.dimen.stream_channel_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.ChannelListView_streamAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.ChannelListView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials));
        avatarInitialTextColor = a.getColor(R.styleable.ChannelListView_streamAvatarTextColor, Color.WHITE);
        avatarInitialTextStyle = a.getInt(R.styleable.ChannelListView_streamAvatarTextStyle, Typeface.BOLD);
        // Read State
        showReadState = a.getBoolean(R.styleable.ChannelListView_streamShowReadState, true);
        readStateAvatarWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateAvatarWidth, getDimension(R.dimen.stream_read_state_avatar_width));
        readStateAvatarHeight = a.getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateAvatarHeight, getDimension(R.dimen.stream_read_state_avatar_height));

        readStateTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateTextSize, getDimension(R.dimen.stream_read_state_text_size));
        readStateTextColor = a.getColor(R.styleable.ChannelListView_streamReadStateTextColor, Color.BLACK);
        readStateTextStyle = a.getColor(R.styleable.ChannelListView_streamReadStateTextStyle, Typeface.NORMAL);

        a.recycle();
    }

    public void setAvatarBorderColor(@ColorRes int color) {
        avatarBorderColor = color;
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

    public String getChannelWithoutNameText() {
        return !TextUtils.isEmpty(channelWithoutNameText) ? channelWithoutNameText : context.getString(R.string.stream_channel_unknown_title);
    }
}
