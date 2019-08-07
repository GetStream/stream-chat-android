package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.getstream.sdk.chat.R;

public class MessageListViewStyle {
    final String TAG = MessageListViewStyle.class.getSimpleName();

    private int messageTextColorMine;
    private int messageTextColorTheirs;
    private Drawable messageBubbleDrawableMine;
    private Drawable messageBubbleDrawableTheirs;
    private Context context;


    public MessageListViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        context = c;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MessageListView, 0, 0);
        messageTextColorMine = a.getColor(R.styleable.MessageListView_messageTextColorMine, -1);
        messageTextColorTheirs = a.getColor(R.styleable.MessageListView_messageTextColorTheirs, -1);
        messageBubbleDrawableMine = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableMine, -1));
        messageBubbleDrawableTheirs = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableTheirs, -1));
        a.recycle();
    }

    @ColorInt
    public int getMessageTextColorMine() {
        return messageTextColorMine;
    }

    public void setMessageTextColorMine(int messageTextColorMine) {
        this.messageTextColorMine = messageTextColorMine;
    }

    protected Drawable getDrawable(@DrawableRes int drawable) {
        return ContextCompat.getDrawable(context, drawable);
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