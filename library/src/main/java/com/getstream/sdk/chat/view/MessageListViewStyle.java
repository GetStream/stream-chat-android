package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

public class MessageListViewStyle extends BaseStyle {
    final String TAG = MessageListViewStyle.class.getSimpleName();
    // Dimension

    private int messageTextColorMine;
    private int messageTextColorTheirs;
    // Drawable
    private Drawable messageBubbleDrawableMine;
    private Drawable messageBubbleDrawableTheirs;


    public MessageListViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        this.setContext(c);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.MessageListView, 0, 0);
        messageTextColorMine = a.getColor(R.styleable.MessageListView_messageTextColorMine, -1);
        messageTextColorTheirs = a.getColor(R.styleable.MessageListView_messageTextColorTheirs, -1);
        messageBubbleDrawableMine = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableMine, -1));
        messageBubbleDrawableTheirs = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableTheirs, -1));

        // Avatar
        avatarWidth = a.getDimension(R.styleable.MessageListView_userAvatarWidth, c.getResources().getDimension(R.dimen.stream_channel_avatar_height));
        avatarHeight = a.getDimension(R.styleable.MessageListView_userAvatarHeight, c.getResources().getDimension(R.dimen.stream_channel_avatar_width));

        avatarBorderWidth = a.getDimension(R.styleable.MessageListView_userAvatarBorderWidth, c.getResources().getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.MessageListView_userAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.MessageListView_userAvatarBackGroundColor, c.getResources().getColor(R.color.user_intials_background));

        avatarInitialTextSize = a.getDimension(R.styleable.MessageListView_userAvatarTextSize, c.getResources().getDimension(R.dimen.stream_channel_initials));
        avatarInitialTextColor = a.getColor(R.styleable.MessageListView_userAvatarTextColor, c.getResources().getColor(R.color.stream_channel_initials));
        avatarInitialTextStyle = a.getInt(R.styleable.MessageListView_userAvatarTextStyle, Typeface.NORMAL);

        a.recycle();
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
}