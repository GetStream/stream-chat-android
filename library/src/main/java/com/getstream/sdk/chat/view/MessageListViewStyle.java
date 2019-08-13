package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

public class MessageListViewStyle extends BaseStyle {
    final String TAG = MessageListViewStyle.class.getSimpleName();
    // Dimension
    private int channelTitleTextSize;
    private int messageTextColorMine;
    private int messageTextColorTheirs;
    // Color
    private int channelTitleTextColor;
    // Style
    private int channelTitleTextStyle;
    // Drawable
    private Drawable messageBubbleDrawableMine;
    private Drawable messageBubbleDrawableTheirs;

    private boolean backButtonShow;

    public MessageListViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        this.setContext(c);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.MessageListView, 0, 0);
        messageTextColorMine = a.getColor(R.styleable.MessageListView_messageTextColorMine, -1);
        messageTextColorTheirs = a.getColor(R.styleable.MessageListView_messageTextColorTheirs, -1);
        messageBubbleDrawableMine = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableMine, -1));
        messageBubbleDrawableTheirs = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableTheirs, -1));

        channelTitleTextSize = (int) a.getDimension(R.styleable.MessageListView_channelTitleTextSize, c.getResources().getDimension(R.dimen.stream_channel_initials));
        channelTitleTextColor = a.getColor(R.styleable.MessageListView_channelTitleTextColor, c.getResources().getColor(R.color.stream_channel_initials));
        channelTitleTextStyle = a.getInt(R.styleable.MessageListView_channelTitleTextStyle, Typeface.NORMAL);

        avatarWidth = a.getDimension(R.styleable.MessageListView_channelAvatarWidth, c.getResources().getDimension(R.dimen.stream_channel_avatar_height));
        avatarHeight = a.getDimension(R.styleable.MessageListView_channelAvatarHeight, c.getResources().getDimension(R.dimen.stream_channel_avatar_width));

        initialsTextSize = a.getDimension(R.styleable.MessageListView_channelAvatarTextSize, c.getResources().getDimension(R.dimen.stream_channel_initials));
        initialsTextColor = a.getColor(R.styleable.MessageListView_channelAvatarTextColor, c.getResources().getColor(R.color.stream_channel_initials));
        initialsTextStyle = a.getInt(R.styleable.MessageListView_channelAvatarTextStyle, Typeface.NORMAL);

        backButtonShow = a.getBoolean(R.styleable.MessageListView_backButtonShow, false);
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

    @ColorInt
    public int getMessageTextColorMine() {
        return messageTextColorMine;
    }

    public void setMessageTextColorMine(int messageTextColorMine) {
        this.messageTextColorMine = messageTextColorMine;
    }



    public int getMessageTextColorTheirs() {
        return messageTextColorTheirs;
    }

    public void setMessageTextColorTheirs(int messageTextColorTheirs) {
        this.messageTextColorTheirs = messageTextColorTheirs;
    }

    public Drawable getMessageBubbleDrawableMine() {
        return messageBubbleDrawableMine;
    }

    public void setMessageBubbleDrawableMine(Drawable messageBubbleDrawableMine) {
        this.messageBubbleDrawableMine = messageBubbleDrawableMine;
    }

    public Drawable getMessageBubbleDrawableTheirs() {
        return messageBubbleDrawableTheirs;
    }

    public void setMessageBubbleDrawableTheirs(Drawable messageBubbleDrawableTheirs) {
        this.messageBubbleDrawableTheirs = messageBubbleDrawableTheirs;
    }

    public boolean isBackButtonShow() {
        return backButtonShow;
    }
}