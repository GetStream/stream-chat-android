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
import com.getstream.sdk.chat.style.TextStyle;

public class ChannelListViewStyle extends BaseStyle {

    final String TAG = ChannelListViewStyle.class.getSimpleName();

    public @LayoutRes
    int channelPreviewLayout;

    public final TextStyle channelTitleText;
    public final TextStyle channelTitleUnreadText;
    public final TextStyle lastMessage;
    public final TextStyle lastMessageUnread;
    public final TextStyle lastMessageDateText;
    public final TextStyle lastMessageDateUnreadText;

    private String channelWithoutNameText;

    public ChannelListViewStyle(Context c, AttributeSet attrs) {
        this.setContext(c);
        TypedArray a = c.obtainStyledAttributes(attrs,
                R.styleable.ChannelListView, 0, 0);

        channelTitleText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelListView_streamChannelTitleTextSize, getDimension(R.dimen.stream_channel_item_title))
                .color(R.styleable.ChannelListView_streamChannelTitleTextColor, getColor(R.color.stream_black))
                .font(R.styleable.ChannelListView_streamChannelTitleTextFontAssets, R.styleable.ChannelListView_streamChannelTitleTextFont)
                .style(R.styleable.ChannelListView_streamChannelTitleTextStyle, Typeface.BOLD)
                .build();

        channelTitleUnreadText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelListView_streamChannelTitleTextSize, getDimension(R.dimen.stream_channel_item_title))
                .color(R.styleable.ChannelListView_streamChannelTitleUnreadTextColor, getColor(R.color.stream_black))
                .font(R.styleable.ChannelListView_streamChannelTitleTextFontAssets, R.styleable.ChannelListView_streamChannelTitleTextFont)
                .style(R.styleable.ChannelListView_streamChannelTitleUnreadTextStyle, Typeface.BOLD)
                .build();

        channelWithoutNameText = a.getString(R.styleable.ChannelListView_streamChannelWithOutNameTitleText);

        lastMessage = new TextStyle.Builder(a)
                .size(R.styleable.ChannelListView_streamLastMessageTextSize, getDimension(R.dimen.stream_channel_item_message))
                .color(R.styleable.ChannelListView_streamLastMessageTextColor, getColor(R.color.stream_gray_dark))
                .font(R.styleable.ChannelListView_streamLastMessageTextFontAssets, R.styleable.ChannelListView_streamLastMessageTextFont)
                .style(R.styleable.ChannelListView_streamLastMessageTextStyle, Typeface.NORMAL)
                .build();

        lastMessageUnread = new TextStyle.Builder(a)
                .size(R.styleable.ChannelListView_streamLastMessageTextSize, getDimension(R.dimen.stream_channel_item_message))
                .color(R.styleable.ChannelListView_streamLastMessageUnreadTextColor, getColor(R.color.stream_black))
                .font(R.styleable.ChannelListView_streamLastMessageTextFontAssets, R.styleable.ChannelListView_streamLastMessageTextFont)
                .style(R.styleable.ChannelListView_streamLastMessageUnreadTextStyle, Typeface.BOLD)
                .build();

        lastMessageDateText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelListView_streamLastMessageDateTextSize, getDimension(R.dimen.stream_channel_item_message_date))
                .color(R.styleable.ChannelListView_streamLastMessageDateTextColor, getColor(R.color.stream_gray_dark))
                .font(R.styleable.ChannelListView_streamLastMessageDateTextFontAssets, R.styleable.ChannelListView_streamLastMessageDateTextFont)
                .style(R.styleable.ChannelListView_streamLastMessageDateTextStyle, Typeface.NORMAL)
                .build();

        lastMessageDateUnreadText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelListView_streamLastMessageDateTextSize, getDimension(R.dimen.stream_channel_item_message_date))
                .color(R.styleable.ChannelListView_streamLastMessageDateTextColor, getColor(R.color.stream_black))
                .font(R.styleable.ChannelListView_streamLastMessageDateTextFontAssets, R.styleable.ChannelListView_streamLastMessageDateTextFont)
                .style(R.styleable.ChannelListView_streamLastMessageDateUnreadTextStyle, Typeface.BOLD)
                .build();

        channelPreviewLayout = a.getResourceId(R.styleable.ChannelListView_streamChannelPreviewLayout, R.layout.stream_item_channel);
        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarWidth, getDimension(R.dimen.stream_channel_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarHeight, getDimension(R.dimen.stream_channel_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.ChannelListView_streamAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.ChannelListView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelListView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials))
                .color(R.styleable.ChannelListView_streamAvatarTextColor, Color.WHITE)
                .font(R.styleable.ChannelListView_streamAvatarTextFontAssets, R.styleable.ChannelListView_streamAvatarTextFont)
                .style(R.styleable.ChannelListView_streamAvatarTextStyle, Typeface.BOLD)
                .build();

        // Read State
        showReadState = a.getBoolean(R.styleable.ChannelListView_streamShowReadState, true);
        readStateAvatarWidth = a.getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateAvatarWidth, getDimension(R.dimen.stream_read_state_avatar_width));
        readStateAvatarHeight = a.getDimensionPixelSize(R.styleable.ChannelListView_streamReadStateAvatarHeight, getDimension(R.dimen.stream_read_state_avatar_height));

        readStateText = new TextStyle.Builder(a)
                .size(R.styleable.ChannelListView_streamReadStateTextSize, getDimension(R.dimen.stream_read_state_text_size))
                .color(R.styleable.ChannelListView_streamReadStateTextColor, Color.BLACK)
                .font(R.styleable.ChannelListView_streamReadStateTextFontAssets, R.styleable.ChannelListView_streamReadStateTextFont)
                .style(R.styleable.ChannelListView_streamReadStateTextStyle, Typeface.BOLD)
                .build();

        a.recycle();
    }

    public void setAvatarBorderColor(@ColorRes int color) {
        avatarBorderColor = color;
    }

    public String getChannelWithoutNameText() {
        return !TextUtils.isEmpty(channelWithoutNameText) ? channelWithoutNameText : context.getString(R.string.stream_channel_unknown_title);
    }

    public int getChannelPreviewLayout() {
        return channelPreviewLayout;
    }

}
