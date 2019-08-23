package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

public class MessageListViewStyle extends BaseStyle {
    private static final String TAG = MessageListViewStyle.class.getSimpleName();

    // Message text
    private int messageTextSizeMine;
    private int messageTextSizeTheirs;
    private int messageTextColorMine;
    private int messageTextColorTheirs;
    private int messageTextStyleMine;
    private int messageTextStyleTheirs;
    // Message Bubble
    private Drawable messageBubbleDrawableMine;
    private Drawable messageBubbleDrawableTheirs;
    private int messageTopLeftCornerRadiusMine;
    private int messageTopRightCornerRadiusMine;
    private int messageBottomRightCornerRadiusMine;
    private int messageBottomLeftCornerRadiusMine;
    private int messageTopLeftCornerRadiusTheirs;
    private int messageTopRightCornerRadiusTheirs;
    private int messageBottomRightCornerRadiusTheirs;
    private int messageBottomLeftCornerRadiusTheirs;
    private int messageBackgroundColorMine;
    private int messageBackgroundColorTheirs;
    private int messageBorderColorMine;
    private int messageBorderColorTheirs;
    private int messageBorderWidthMine;
    private int messageBorderWidthTheirs;
    // Reaction
    private boolean enableReaction;
    private boolean showUsersReactionDlg;
    private Drawable reactionDlgBgDrawable;
    private int reactionDlgBgColor;
    private int reactionDlgEmojiSize;
    private int reactionDlgEmojiMargin;
    // Thread
    private boolean enableThread;

    public MessageListViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        setContext(c);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.MessageListView, 0, 0);

        // Message Text
        messageTextSizeMine = a.getDimensionPixelSize(R.styleable.MessageListView_messageTextSizeMine, getDimension(R.dimen.message_text_font_size));
        messageTextColorMine = a.getColor(R.styleable.MessageListView_messageTextColorMine, Color.BLACK);
        messageTextStyleMine = a.getColor(R.styleable.MessageListView_messageTextStyleMine, Typeface.NORMAL);

        messageTextSizeTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_messageTextSizeTheirs, getDimension(R.dimen.message_text_font_size));
        messageTextColorTheirs = a.getColor(R.styleable.MessageListView_messageTextColorTheirs, Color.BLACK);
        messageTextStyleTheirs = a.getColor(R.styleable.MessageListView_messageTextStyleTheirs, Typeface.NORMAL);
        // Message Bubble
        messageBubbleDrawableMine = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableMine, -1));
        messageBubbleDrawableTheirs = getDrawable(a.getResourceId(R.styleable.MessageListView_messageBubbleDrawableTheirs, -1));

        messageTopLeftCornerRadiusMine = a.getDimensionPixelSize(R.styleable.MessageListView_messageTopLeftCornerRadiusMine, getDimension(R.dimen.message_corner_radius1));
        messageTopRightCornerRadiusMine = a.getDimensionPixelSize(R.styleable.MessageListView_messageTopRightCornerRadiusMine, getDimension(R.dimen.message_corner_radius1));
        messageBottomRightCornerRadiusMine = a.getDimensionPixelSize(R.styleable.MessageListView_messageBottomRightCornerRadiusMine, getDimension(R.dimen.message_corner_radius2));
        messageBottomLeftCornerRadiusMine = a.getDimensionPixelSize(R.styleable.MessageListView_messageBottomLeftCornerRadiusMine, getDimension(R.dimen.message_corner_radius1));

        messageTopLeftCornerRadiusTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_messageTopLeftCornerRadiusTheirs, getDimension(R.dimen.message_corner_radius1));
        messageTopRightCornerRadiusTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_messageTopRightCornerRadiusTheirs, getDimension(R.dimen.message_corner_radius1));
        messageBottomRightCornerRadiusTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_messageBottomRightCornerRadiusTheirs, getDimension(R.dimen.message_corner_radius1));
        messageBottomLeftCornerRadiusTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_messageBottomLeftCornerRadiusTheirs, getDimension(R.dimen.message_corner_radius2));

        messageBackgroundColorMine = a.getColor(R.styleable.MessageListView_messageBackgroundColorMine, getColor(R.color.message_background_outgoing));
        messageBackgroundColorTheirs = a.getColor(R.styleable.MessageListView_messageBackgroundColorTheirs, getColor(R.color.message_background_incoming));

        messageBorderColorMine = a.getColor(R.styleable.MessageListView_messageBorderColorMine, getColor(R.color.message_stroke));
        messageBorderColorTheirs = a.getColor(R.styleable.MessageListView_messageBorderColorTheirs, getColor(R.color.message_stroke));

        messageBorderWidthMine = a.getDimensionPixelSize(R.styleable.MessageListView_messageBorderWidthMine, getDimension(R.dimen.message_stroke));
        messageBorderWidthTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_messageBorderWidthTheirs, getDimension(R.dimen.message_stroke));
        // Reaction Dialog
        enableReaction = a.getBoolean(R.styleable.MessageListView_enableReaction, true);
        showUsersReactionDlg = a.getBoolean(R.styleable.MessageListView_showUsersReactionDlg, true);
        reactionDlgBgDrawable = getDrawable(a.getResourceId(R.styleable.MessageListView_reactionDlgBgDrawable, -1));
        reactionDlgBgColor = a.getColor(R.styleable.MessageListView_reactionDlgbgColor, getColor(R.color.reaction_dialog_background));
        reactionDlgEmojiSize = a.getDimensionPixelSize(R.styleable.MessageListView_reactionDlgEmojiSize, getDimension(R.dimen.reaction_dialog_emoji_size));
        reactionDlgEmojiMargin = a.getDimensionPixelSize(R.styleable.MessageListView_reactionDlgEmojiMargin, getDimension(R.dimen.reaction_dialog_emoji_margin));
        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.MessageListView_avatarWidth, getDimension(R.dimen.message_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.MessageListView_avatarHeight, getDimension(R.dimen.message_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.MessageListView_avatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.MessageListView_avatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.MessageListView_avatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_avatarTextSize, getDimension(R.dimen.stream_channel_initials));
        avatarInitialTextColor = a.getColor(R.styleable.MessageListView_avatarTextColor, Color.WHITE);
        avatarInitialTextStyle = a.getInt(R.styleable.MessageListView_avatarTextStyle, Typeface.BOLD);

        // Read State
        showReadState = a.getBoolean(R.styleable.MessageListView_showReadState, true);
        readStateAvatarWidth = a.getDimensionPixelSize(R.styleable.MessageListView_readStateAvatarWidth, getDimension(R.dimen.read_state_avatar_width));
        readStateAvatarHeight = a.getDimensionPixelSize(R.styleable.MessageListView_readStateAvatarHeight, getDimension(R.dimen.read_state_avatar_height));

        readStateTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_readStateTextSize, getDimension(R.dimen.read_state_text_size));
        readStateTextColor = a.getColor(R.styleable.MessageListView_readStateTextColor, Color.WHITE);
        readStateTextStyle = a.getColor(R.styleable.MessageListView_readStateTextStyle, Typeface.BOLD);
        // Thread
        enableThread = a.getBoolean(R.styleable.MessageListView_enableThread, true);
        a.recycle();
    }

    public int getMessageTextSizeMine() {
        return messageTextSizeMine;
    }

    public int getMessageTextSizeTheirs() {
        return messageTextSizeTheirs;
    }

    public int getMessageTextColorMine() {
        return messageTextColorMine;
    }

    public int getMessageTextColorTheirs() {
        return messageTextColorTheirs;
    }

    public int getMessageTextStyleMine() {
        return messageTextStyleMine;
    }

    public int getMessageTextStyleTheirs() {
        return messageTextStyleTheirs;
    }

    public Drawable getMessageBubbleDrawableMine() {
        return messageBubbleDrawableMine;
    }

    public Drawable getMessageBubbleDrawableTheirs() {
        return messageBubbleDrawableTheirs;
    }

    public int getMessageTopLeftCornerRadiusMine() {
        return messageTopLeftCornerRadiusMine;
    }

    public int getMessageTopRightCornerRadiusMine() {
        return messageTopRightCornerRadiusMine;
    }

    public int getMessageBottomRightCornerRadiusMine() {
        return messageBottomRightCornerRadiusMine;
    }

    public int getMessageBottomLeftCornerRadiusMine() {
        return messageBottomLeftCornerRadiusMine;
    }

    public int getMessageTopLeftCornerRadiusTheirs() {
        return messageTopLeftCornerRadiusTheirs;
    }

    public int getMessageTopRightCornerRadiusTheirs() {
        return messageTopRightCornerRadiusTheirs;
    }

    public int getMessageBottomRightCornerRadiusTheirs() {
        return messageBottomRightCornerRadiusTheirs;
    }

    public int getMessageBottomLeftCornerRadiusTheirs() {
        return messageBottomLeftCornerRadiusTheirs;
    }

    public int getMessageBackgroundColorMine() {
        return messageBackgroundColorMine;
    }

    public int getMessageBackgroundColorTheirs() {
        return messageBackgroundColorTheirs;
    }

    public int getMessageBorderColorMine() {
        return messageBorderColorMine;
    }

    public int getMessageBorderColorTheirs() {
        return messageBorderColorTheirs;
    }

    public int getMessageBorderWidthMine() {
        return messageBorderWidthMine;
    }

    public int getMessageBorderWidthTheirs() {
        return messageBorderWidthTheirs;
    }

    // Reaction Dialog

    public boolean isEnableReaction() {
        return enableReaction;
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

    // Thread

    public boolean isEnableThread() {
        return enableThread;
    }
}