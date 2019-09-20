package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.getstream.sdk.chat.R;

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
    // Attachment
    private int attachmentTitleTextSize;
    private int attachmentTitleTextColor;
    private int attachmentTitleTextStyle;

    private int attachmentDescriptionTextSize;
    private int attachmentDescriptionTextColor;
    private int attachmentDescriptionTextStyle;

    private int attachmentFileSizeTextSize;
    private int attachmentFileSizeTextColor;
    private int attachmentFileSizeTextStyle;
    // Reaction
    private boolean enableReaction;
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
        messageTextSizeMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageTextSizeMine, getDimension(R.dimen.stream_message_text_font_size));
        messageTextColorMine = a.getColor(R.styleable.MessageListView_streamMessageTextColorMine, Color.BLACK);
        messageTextStyleMine = a.getColor(R.styleable.MessageListView_streamMessageTextStyleMine, Typeface.NORMAL);

        messageTextSizeTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageTextSizeTheirs, getDimension(R.dimen.stream_message_text_font_size));
        messageTextColorTheirs = a.getColor(R.styleable.MessageListView_streamMessageTextColorTheirs, Color.BLACK);
        messageTextStyleTheirs = a.getColor(R.styleable.MessageListView_streamMessageTextStyleTheirs, Typeface.NORMAL);
        // Message Bubble
        messageBubbleDrawableMine = getDrawable(a.getResourceId(R.styleable.MessageListView_streamMessageBubbleDrawableMine, -1));
        messageBubbleDrawableTheirs = getDrawable(a.getResourceId(R.styleable.MessageListView_streamMessageBubbleDrawableTheirs, -1));

        messageTopLeftCornerRadiusMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageTopLeftCornerRadiusMine, getDimension(R.dimen.stream_message_corner_radius1));
        messageTopRightCornerRadiusMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageTopRightCornerRadiusMine, getDimension(R.dimen.stream_message_corner_radius1));
        messageBottomRightCornerRadiusMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageBottomRightCornerRadiusMine, getDimension(R.dimen.stream_message_corner_radius2));
        messageBottomLeftCornerRadiusMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageBottomLeftCornerRadiusMine, getDimension(R.dimen.stream_message_corner_radius1));

        messageTopLeftCornerRadiusTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageTopLeftCornerRadiusTheirs, getDimension(R.dimen.stream_message_corner_radius1));
        messageTopRightCornerRadiusTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageTopRightCornerRadiusTheirs, getDimension(R.dimen.stream_message_corner_radius1));
        messageBottomRightCornerRadiusTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageBottomRightCornerRadiusTheirs, getDimension(R.dimen.stream_message_corner_radius1));
        messageBottomLeftCornerRadiusTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageBottomLeftCornerRadiusTheirs, getDimension(R.dimen.stream_message_corner_radius2));

        messageBackgroundColorMine = a.getColor(R.styleable.MessageListView_streamMessageBackgroundColorMine, getColor(R.color.stream_message_background_outgoing));
        messageBackgroundColorTheirs = a.getColor(R.styleable.MessageListView_streamMessageBackgroundColorTheirs, getColor(R.color.stream_message_background_incoming));

        messageBorderColorMine = a.getColor(R.styleable.MessageListView_streamMessageBorderColorMine, getColor(R.color.stream_message_stroke));
        messageBorderColorTheirs = a.getColor(R.styleable.MessageListView_streamMessageBorderColorTheirs, getColor(R.color.stream_message_stroke));

        messageBorderWidthMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageBorderWidthMine, getDimension(R.dimen.stream_message_stroke));
        messageBorderWidthTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageBorderWidthTheirs, getDimension(R.dimen.stream_message_stroke));
        // Attachment
        attachmentTitleTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentTitleTextSize, getDimension(R.dimen.stream_attach_title_text));
        attachmentTitleTextColor = a.getColor(R.styleable.MessageListView_streamAttachmentTitleTextColor, getColor(R.color.stream_attach_title_text));
        attachmentTitleTextStyle = a.getColor(R.styleable.MessageListView_streamAttachmentTitleTextStyle, Typeface.BOLD);

        attachmentDescriptionTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentDescriptionTextSize, getDimension(R.dimen.stream_attach_description_text));
        attachmentDescriptionTextColor = a.getColor(R.styleable.MessageListView_streamAttachmentDescriptionTextColor, getColor(R.color.stream_attach_description_text));
        attachmentDescriptionTextStyle = a.getColor(R.styleable.MessageListView_streamAttachmentDescriptionTextStyle, Typeface.NORMAL);

        attachmentFileSizeTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentFileSizeTextSize, getDimension(R.dimen.stream_attach_file_size_text));
        attachmentFileSizeTextColor = a.getColor(R.styleable.MessageListView_streamAttachmentFileSizeTextColor, getColor(R.color.stream_attach_file_size_text));
        attachmentFileSizeTextStyle = a.getColor(R.styleable.MessageListView_streamAttachmentFileSizeTextStyle, Typeface.BOLD);

        // Reaction Dialog
        enableReaction = a.getBoolean(R.styleable.MessageListView_streamEnableReaction, true);
        reactionDlgBgDrawable = getDrawable(a.getResourceId(R.styleable.MessageListView_streamrReactionDlgBgDrawable, -1));
        reactionDlgBgColor = a.getColor(R.styleable.MessageListView_streamReactionDlgbgColor, getColor(R.color.stream_reaction_dialog_background));
        reactionDlgEmojiSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamReactionDlgEmojiSize, getDimension(R.dimen.stream_reaction_dialog_emoji_size));
        reactionDlgEmojiMargin = a.getDimensionPixelSize(R.styleable.MessageListView_streamReactionDlgEmojiMargin, getDimension(R.dimen.stream_reaction_dialog_emoji_margin));
        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.MessageListView_streamAvatarWidth, getDimension(R.dimen.stream_message_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.MessageListView_streamAvatarHeight, getDimension(R.dimen.stream_message_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.MessageListView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.MessageListView_streamAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.MessageListView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials));
        avatarInitialTextColor = a.getColor(R.styleable.MessageListView_streamAvatarTextColor, Color.WHITE);
        avatarInitialTextStyle = a.getInt(R.styleable.MessageListView_streamAvatarTextStyle, Typeface.BOLD);

        // Read State
        showReadState = a.getBoolean(R.styleable.MessageListView_streamShowReadState, true);
        readStateAvatarWidth = a.getDimensionPixelSize(R.styleable.MessageListView_streamReadStateAvatarWidth, getDimension(R.dimen.stream_read_state_avatar_width));
        readStateAvatarHeight = a.getDimensionPixelSize(R.styleable.MessageListView_streamReadStateAvatarHeight, getDimension(R.dimen.stream_read_state_avatar_height));

        readStateTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamReadStateTextSize, getDimension(R.dimen.stream_read_state_text_size));
        readStateTextColor = a.getColor(R.styleable.MessageListView_streamReadStateTextColor, Color.BLACK);
        readStateTextStyle = a.getColor(R.styleable.MessageListView_streamReadStateTextStyle, Typeface.BOLD);
        // Thread
        enableThread = a.getBoolean(R.styleable.MessageListView_streamEnableThread, true);
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

    // Attachment
    public int getAttachmentTitleTextSize() {
        return attachmentTitleTextSize;
    }

    public int getAttachmentTitleTextColor() {
        return attachmentTitleTextColor;
    }

    public int getAttachmentTitleTextStyle() {
        return attachmentTitleTextStyle;
    }

    public int getAttachmentDescriptionTextSize() {
        return attachmentDescriptionTextSize;
    }

    public int getAttachmentDescriptionTextColor() {
        return attachmentDescriptionTextColor;
    }

    public int getAttachmentDescriptionTextStyle() {
        return attachmentDescriptionTextStyle;
    }

    public int getAttachmentFileSizeTextSize() {
        return attachmentFileSizeTextSize;
    }

    public int getAttachmentFileSizeTextColor() {
        return attachmentFileSizeTextColor;
    }

    public int getAttachmentFileSizeTextStyle() {
        return attachmentFileSizeTextStyle;
    }


    // Reaction Dialog

    public boolean isEnableReaction() {
        return enableReaction;
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