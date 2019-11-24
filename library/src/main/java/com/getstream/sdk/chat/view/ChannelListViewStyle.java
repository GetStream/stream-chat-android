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
    // Channel Title
    private int channelTitleTextSize;
    private int channelTitleTextColor;
    private int channelTitleUnreadTextColor;
    private String channelTitleTextFontPath;
    private int channelTitleTextStyle;
    private int channelTitleUnreadTextStyle;
    private String channelWithoutNameText;
    // Last Message
    private int lastMessageTextSize;
    private int lastMessageTextColor;
    private int lastMessageUnreadTextColor;
    private String lastMessageTextFontPath;
    private int lastMessageTextStyle;
    private int lastMessageUnreadTextStyle;
    // Last Message Date
    private int lastMessageDateTextSize;
    private int lastMessageDateTextColor;
    private int lastMessageDateUnreadTextColor;
    private String lastMessageDateTextFontPath;
    private int lastMessageDateTextStyle;
    private int lastMessageDateUnreadTextStyle;

    public ChannelListViewStyle(Context c, AttributeSet attrs) {
        this.setContext(c);
        TypedArray a = c.obtainStyledAttributes(attrs,
                R.styleable.ChannelListView, 0, 0);

        channelTitleTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamChannelTitleTextSize, getDimension(R.dimen.stream_channel_item_title));
        channelTitleTextColor = a.getColor(R.styleable.ChannelListView_streamChannelTitleTextColor, getColor(R.color.stream_black));
        channelTitleUnreadTextColor = a.getColor(R.styleable.ChannelListView_streamChannelTitleUnreadTextColor, getColor(R.color.stream_black));
        channelTitleTextFontPath = a.getString(R.styleable.ChannelListView_streamChannelTitleTextFontPath);
        channelTitleTextStyle = a.getInt(R.styleable.ChannelListView_streamChannelTitleTextStyle, Typeface.BOLD);
        channelTitleUnreadTextStyle = a.getInt(R.styleable.ChannelListView_streamChannelTitleUnreadTextStyle, Typeface.BOLD);
        channelWithoutNameText = a.getString(R.styleable.ChannelListView_streamChannelWithOutNameTitleText);

        lastMessageTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamLastMessageTextSize, getDimension(R.dimen.stream_channel_item_message));
        lastMessageTextColor = a.getColor(R.styleable.ChannelListView_streamLastMessageTextColor, getColor(R.color.stream_gray_dark));
        lastMessageUnreadTextColor = a.getColor(R.styleable.ChannelListView_streamLastMessageUnreadTextColor, getColor(R.color.stream_black));
        lastMessageTextFontPath = a.getString(R.styleable.ChannelListView_streamLastMessageTextFontPath);
        lastMessageTextStyle = a.getInt(R.styleable.ChannelListView_streamLastMessageTextStyle, Typeface.NORMAL);
        lastMessageUnreadTextStyle = a.getInt(R.styleable.ChannelListView_streamLastMessageUnreadTextStyle, Typeface.BOLD);

        lastMessageDateTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamLastMessageDateTextSize, getDimension(R.dimen.stream_channel_item_message_date));
        lastMessageDateTextColor = a.getColor(R.styleable.ChannelListView_streamLastMessageDateTextColor, getColor(R.color.stream_gray_dark));
        lastMessageDateUnreadTextColor = a.getColor(R.styleable.ChannelListView_streamLastMessageDateTextColor, getColor(R.color.stream_black));
        lastMessageDateTextFontPath = a.getString(R.styleable.ChannelListView_streamLastMessageDateTextFontPath);
        lastMessageDateTextStyle = a.getInt(R.styleable.ChannelListView_streamLastMessageDateTextStyle, Typeface.NORMAL);
        lastMessageDateUnreadTextStyle = a.getInt(R.styleable.ChannelListView_streamLastMessageDateUnreadTextStyle, Typeface.BOLD);

        channelPreviewLayout = a.getResourceId(R.styleable.ChannelListView_streamChannelPreviewLayout, R.layout.stream_item_channel);
        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarWidth, getDimension(R.dimen.stream_channel_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarHeight, getDimension(R.dimen.stream_channel_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.ChannelListView_streamAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.ChannelListView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials));
        avatarInitialTextColor = a.getColor(R.styleable.ChannelListView_streamAvatarTextColor, Color.WHITE);
        avatarInitialTextFontPath = a.getString(R.styleable.ChannelListView_streamAvatarTextFontPath);
        avatarInitialTextStyle = a.getInt(R.styleable.ChannelListView_streamAvatarTextStyle, Typeface.BOLD);
        // Read State
        showReadState = a.getBoolean(R.styleable.ChannelListView_streamShowReadState, true);
        readStateAvatarWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateAvatarWidth, getDimension(R.dimen.stream_read_state_avatar_width));
        readStateAvatarHeight = a.getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateAvatarHeight, getDimension(R.dimen.stream_read_state_avatar_height));

        readStateTextSize = a.getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateTextSize, getDimension(R.dimen.stream_read_state_text_size));
        readStateTextColor = a.getColor(R.styleable.ChannelListView_streamReadStateTextColor, Color.BLACK);
        readStateTextFontPath = a.getString(R.styleable.ChannelListView_streamReadStateTextFontPath);
        readStateTextStyle = a.getInt(R.styleable.ChannelListView_streamReadStateTextStyle, Typeface.NORMAL);

        a.recycle();
    }

    public void setAvatarBorderColor(@ColorRes int color) {
        avatarBorderColor = color;
    }

    public int getChannelTitleTextSize() {
        return channelTitleTextSize;
    }

    public int getChannelTitleTextColor() {
        return channelTitleTextColor;
    }

    public String getChannelTitleTextFontPath() {
        return channelTitleTextFontPath;
    }

    public int getChannelTitleTextStyle() {
        return channelTitleTextStyle;
    }

    public int getChannelTitleUnreadTextStyle() {
        return channelTitleUnreadTextStyle;
    }

    public int getChannelTitleUnreadTextColor() {
        return channelTitleUnreadTextColor;
    }

    public String getChannelWithoutNameText() {
        return !TextUtils.isEmpty(channelWithoutNameText) ? channelWithoutNameText : context.getString(R.string.stream_channel_unknown_title);
    }

    public int getLastMessageTextSize() {
        return lastMessageTextSize;
    }

    public int getLastMessageTextColor() {
        return lastMessageTextColor;
    }

    public String getLastMessageTextFontPath() {
        return lastMessageTextFontPath;
    }

    public int getLastMessageTextStyle() {
        return lastMessageTextStyle;
    }

    public int getLastMessageUnreadTextColor() {
        return lastMessageUnreadTextColor;
    }

    public int getLastMessageUnreadTextStyle() {
        return lastMessageUnreadTextStyle;
    }

    public int getLastMessageDateTextSize() {
        return lastMessageDateTextSize;
    }

    public int getLastMessageDateTextColor() {
        return lastMessageDateTextColor;
    }

    public int getLastMessageDateUnreadTextColor() {
        return lastMessageDateUnreadTextColor;
    }

    public String getLastMessageDateTextFontPath() {
        return lastMessageDateTextFontPath;
    }

    public int getLastMessageDateTextStyle() {
        return lastMessageDateTextStyle;
    }

    public int getLastMessageDateUnreadTextStyle() {
        return lastMessageDateUnreadTextStyle;
    }

    public int getChannelPreviewLayout() {
        return channelPreviewLayout;
    }

}
