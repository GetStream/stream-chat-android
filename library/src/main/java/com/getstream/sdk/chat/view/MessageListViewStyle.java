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
    // Reaction
    private boolean showReactionDlg;
    private boolean showUsersReactionDlg;
    private Drawable reactionDlgBgDrawable;
    private int reactionDlgBgColor;
    private int reactionDlgEmojiSize;
    private int reactionDlgEmojiMargin;

    public MessageListViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        setContext(c);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.MessageListView, 0, 0);

        messageTextColorMine = a.getColor(R.styleable.MessageListView_messageTextColorMine, Color.BLACK);
        messageTextColorTheirs = a.getColor(R.styleable.MessageListView_messageTextColorTheirs, Color.BLACK);
        messageBubbleDrawableMine = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableMine, R.drawable.message_bubble_mine));
        messageBubbleDrawableTheirs = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableTheirs, R.drawable.message_bubble_theirs));

        // Reaction Dialog
        showReactionDlg = a.getBoolean(R.styleable.MessageListView_showReactionDlg, true);
        showUsersReactionDlg = a.getBoolean(R.styleable.MessageListView_showUsersReactionDlg, true);
        reactionDlgBgDrawable = getDrawable(a.getResourceId(R.styleable.MessageListView_reactionDlgBgDrawable, R.drawable.round_dialog_reaction));
        reactionDlgBgColor = a.getColor(R.styleable.MessageListView_reactionDlgbgColor, Color.BLACK);
        reactionDlgEmojiSize = a.getDimensionPixelSize(R.styleable.MessageListView_reactionDlgEmojiSize, getDimension(R.dimen.reaction_dialog_emoji_size));
        reactionDlgEmojiMargin = a.getDimensionPixelSize(R.styleable.MessageListView_reactionDlgEmojiMargin, getDimension(R.dimen.reaction_dialog_emoji_margin));
        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.MessageListView_userAvatarWidth, getDimension(R.dimen.message_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.MessageListView_userAvatarHeight, getDimension(R.dimen.message_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.MessageListView_userAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.MessageListView_userAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.MessageListView_userAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_userAvatarTextSize, getDimension(R.dimen.stream_channel_initials));
        avatarInitialTextColor = a.getColor(R.styleable.MessageListView_userAvatarTextColor, Color.WHITE);
        avatarInitialTextStyle = a.getInt(R.styleable.MessageListView_userAvatarTextStyle, Typeface.BOLD);

        // Read State
        showReadState = a.getBoolean(R.styleable.MessageListView_userShowReadState, true);
        readStateAvatarWidth = a.getDimensionPixelSize(R.styleable.MessageListView_userReadStateAvatarWidth, getDimension(R.dimen.read_state_avatar_width));
        readStateAvatarHeight = a.getDimensionPixelSize(R.styleable.MessageListView_userReadStateAvatarHeight, getDimension(R.dimen.read_state_avatar_height));

        readStateTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_userRreadStateTextSize, getDimension(R.dimen.read_state_text_size));
        readStateTextColor = a.getColor(R.styleable.MessageListView_userReadStateTextColor, Color.BLACK);
        readStateTextStyle = a.getColor(R.styleable.MessageListView_userReadStateTextStyle, Typeface.NORMAL);

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

    // Reaction Dialog

    public boolean isShowReactionDlg() {
        return showReactionDlg;
    }

    public boolean isShowUsersReactionDlg() {
        return showUsersReactionDlg;
    }

    public Drawable getReactionDlgBgDrawable() {
        return reactionDlgBgDrawable;
    }

    public int getReactionDlgBgColor() {
        return reactionDlgBgColor;
    }

    public int getReactionDlgEmojiSize() {
        return reactionDlgEmojiSize;
    }

    public int getReactionDlgEmojiMargin() {
        return reactionDlgEmojiMargin;
    }
}