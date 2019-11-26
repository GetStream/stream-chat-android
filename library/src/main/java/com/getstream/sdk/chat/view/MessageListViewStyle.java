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
    private String messageTextFontPathMine;
    private String messageTextFontPathTheirs;
    // Message Bubble
    private int messageBubbleDrawableMine;
    private int messageBubbleDrawableTheirs;
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
    private String attachmentTitleTextFontPath;
    private String attachmentDescriptionTextFontPath;
    private String attachmentFileSizeTextFontPath;
    private int attachmentTitleTextSizeMine;
    private int attachmentTitleTextColorMine;
    private int attachmentTitleTextStyleMine;

    private int attachmentTitleTextSizeTheirs;
    private int attachmentTitleTextColorTheirs;
    private int attachmentTitleTextStyleTheirs;

    private int attachmentDescriptionTextSizeMine;
    private int attachmentDescriptionTextColorMine;
    private int attachmentDescriptionTextStyleMine;

    private int attachmentDescriptionTextSizeTheirs;
    private int attachmentDescriptionTextColorTheirs;
    private int attachmentDescriptionTextStyleTheirs;

    private int attachmentFileSizeTextSizeMine;
    private int attachmentFileSizeTextColorMine;
    private int attachmentFileSizeTextStyleMine;

    private int attachmentFileSizeTextSizeTheirs;
    private int attachmentFileSizeTextColorTheirs;
    private int attachmentFileSizeTextStyleTheirs;
    // Reaction
    private boolean reactionEnabled;
    // ReactionView
    private int reactionViewBgDrawable;
    private int reactionViewBgColor;
    private int reactionViewEmojiSize;
    private int reactionViewEmojiMargin;
    // ReactionInput
    private int reactionInputBgColor;
    private int reactionInputEmojiSize;
    private int reactionInputEmojiMargin;

    private boolean threadEnabled;
    private boolean userNameShow;
    private boolean messageDateShow;

    // Date Separator
    private int dateSeparatorDateTextSize;
    private int dateSeparatorDateTextColor;
    private String dateSeparatorDateTextFontPath;
    private int dateSeparatorDateTextStyle;
    private int dateSeparatorLineColor;
    private int dateSeparatorLineWidth;
    private int dateSeparatorLineDrawable;

    public MessageListViewStyle(Context c, AttributeSet attrs) {
        // parse the attributes
        setContext(c);
        TypedArray a = this.getContext().obtainStyledAttributes(attrs,
                R.styleable.MessageListView, 0, 0);

        // Message Text
        messageTextSizeMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageTextSizeMine, getDimension(R.dimen.stream_message_text_font_size));
        messageTextColorMine = a.getColor(R.styleable.MessageListView_streamMessageTextColorMine, Color.BLACK);
        messageTextFontPathMine = a.getString(R.styleable.MessageListView_streamMessageTextFontPathMine);
        messageTextStyleMine = a.getInt(R.styleable.MessageListView_streamMessageTextStyleMine, Typeface.NORMAL);

        messageTextSizeTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamMessageTextSizeTheirs, getDimension(R.dimen.stream_message_text_font_size));
        messageTextColorTheirs = a.getColor(R.styleable.MessageListView_streamMessageTextColorTheirs, Color.BLACK);
        messageTextFontPathTheirs = a.getString(R.styleable.MessageListView_streamMessageTextFontPathTheirs);
        messageTextStyleTheirs = a.getInt(R.styleable.MessageListView_streamMessageTextStyleTheirs, Typeface.NORMAL);
        // Message Bubble
        messageBubbleDrawableMine = a.getResourceId(R.styleable.MessageListView_streamMessageBubbleDrawableMine, -1);
        messageBubbleDrawableTheirs = a.getResourceId(R.styleable.MessageListView_streamMessageBubbleDrawableTheirs, -1);

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
        attachmentTitleTextFontPath = a.getString(R.styleable.MessageListView_streamAttachmentTitleTextFontPath);
        attachmentDescriptionTextFontPath = a.getString(R.styleable.MessageListView_streamAttachmentDescriptionTextFontPath);
        attachmentFileSizeTextFontPath = a.getString(R.styleable.MessageListView_streamAttachmentFileSizeTextFontPath);
        attachmentTitleTextSizeMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentTitleTextSizeMine, getDimension(R.dimen.stream_attach_title_text));
        attachmentTitleTextColorMine = a.getColor(R.styleable.MessageListView_streamAttachmentTitleTextColorMine, getColor(R.color.stream_attach_title_text));
        attachmentTitleTextStyleMine = a.getInt(R.styleable.MessageListView_streamAttachmentTitleTextStyleMine, Typeface.BOLD);

        attachmentTitleTextSizeTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentTitleTextSizeTheirs, getDimension(R.dimen.stream_attach_title_text));
        attachmentTitleTextColorTheirs = a.getColor(R.styleable.MessageListView_streamAttachmentTitleTextColorTheirs, getColor(R.color.stream_attach_title_text));
        attachmentTitleTextStyleTheirs = a.getInt(R.styleable.MessageListView_streamAttachmentTitleTextStyleTheirs, Typeface.BOLD);

        attachmentDescriptionTextSizeMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentDescriptionTextSizeMine, getDimension(R.dimen.stream_attach_description_text));
        attachmentDescriptionTextColorMine = a.getColor(R.styleable.MessageListView_streamAttachmentDescriptionTextColorMine, getColor(R.color.stream_gray_dark));
        attachmentDescriptionTextStyleMine = a.getInt(R.styleable.MessageListView_streamAttachmentDescriptionTextStyleMine, Typeface.NORMAL);

        attachmentDescriptionTextSizeTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentDescriptionTextSizeTheirs, getDimension(R.dimen.stream_attach_description_text));
        attachmentDescriptionTextColorTheirs = a.getColor(R.styleable.MessageListView_streamAttachmentDescriptionTextColorTheirs, getColor(R.color.stream_gray_dark));
        attachmentDescriptionTextStyleTheirs = a.getInt(R.styleable.MessageListView_streamAttachmentDescriptionTextStyleTheirs, Typeface.NORMAL);

        attachmentFileSizeTextSizeMine = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentFileSizeTextSizeMine, getDimension(R.dimen.stream_attach_file_size_text));
        attachmentFileSizeTextColorMine = a.getColor(R.styleable.MessageListView_streamAttachmentFileSizeTextColorMine, getColor(R.color.stream_gray_dark));
        attachmentFileSizeTextStyleMine = a.getInt(R.styleable.MessageListView_streamAttachmentFileSizeTextStyleMine, Typeface.BOLD);

        attachmentFileSizeTextSizeTheirs = a.getDimensionPixelSize(R.styleable.MessageListView_streamAttachmentFileSizeTextSizeTheirs, getDimension(R.dimen.stream_attach_file_size_text));
        attachmentFileSizeTextColorTheirs = a.getColor(R.styleable.MessageListView_streamAttachmentFileSizeTextColorTheirs, getColor(R.color.stream_gray_dark));
        attachmentFileSizeTextStyleTheirs = a.getInt(R.styleable.MessageListView_streamAttachmentFileSizeTextStyleTheirs, Typeface.BOLD);

        // Reaction
        reactionEnabled = a.getBoolean(R.styleable.MessageListView_streamReactionEnabled, true);

        reactionViewBgDrawable = a.getResourceId(R.styleable.MessageListView_streamrReactionViewBgDrawable, -1);
        reactionViewBgColor = a.getColor(R.styleable.MessageListView_streamReactionViewBgColor, getColor(R.color.stream_reaction_input_background));
        reactionViewEmojiSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamReactionViewEmojiSize, getDimension(R.dimen.stream_reaction_view_emoji_size));
        reactionViewEmojiMargin = a.getDimensionPixelSize(R.styleable.MessageListView_streamReactionViewEmojiMargin, getDimension(R.dimen.stream_reaction_view_emoji_margin));
        // Reaction Dialog
        reactionInputBgColor = a.getColor(R.styleable.MessageListView_streamReactionInputbgColor, getColor(R.color.stream_reaction_input_background));
        reactionInputEmojiSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamReactionInputEmojiSize, getDimension(R.dimen.stream_reaction_input_emoji_size));
        reactionInputEmojiMargin = a.getDimensionPixelSize(R.styleable.MessageListView_streamReactionInputEmojiMargin, getDimension(R.dimen.stream_reaction_input_emoji_margin));

        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.MessageListView_streamAvatarWidth, getDimension(R.dimen.stream_message_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.MessageListView_streamAvatarHeight, getDimension(R.dimen.stream_message_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.MessageListView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.MessageListView_streamAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.MessageListView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials));
        avatarInitialTextColor = a.getColor(R.styleable.MessageListView_streamAvatarTextColor, Color.WHITE);
        avatarInitialTextFontPath = a.getString(R.styleable.MessageListView_streamAvatarTextFontPath);
        avatarInitialTextStyle = a.getInt(R.styleable.MessageListView_streamAvatarTextStyle, Typeface.BOLD);

        // Read State
        showReadState = a.getBoolean(R.styleable.MessageListView_streamShowReadState, true);
        readStateAvatarWidth = a.getDimensionPixelSize(R.styleable.MessageListView_streamReadStateAvatarWidth, getDimension(R.dimen.stream_read_state_avatar_width));
        readStateAvatarHeight = a.getDimensionPixelSize(R.styleable.MessageListView_streamReadStateAvatarHeight, getDimension(R.dimen.stream_read_state_avatar_height));
        readStateTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamReadStateTextSize, getDimension(R.dimen.stream_read_state_text_size));
        readStateTextColor = a.getColor(R.styleable.MessageListView_streamReadStateTextColor, Color.BLACK);
        readStateTextFontPath = a.getString(R.styleable.MessageListView_streamReadStateTextFontPath);
        readStateTextStyle = a.getColor(R.styleable.MessageListView_streamReadStateTextStyle, Typeface.BOLD);

        threadEnabled = a.getBoolean(R.styleable.MessageListView_streamThreadEnabled, true);

        dateSeparatorDateTextSize = a.getDimensionPixelSize(R.styleable.MessageListView_streamDateSeparatorDateTextSize, getDimension(R.dimen.stream_date_separator_text));
        dateSeparatorDateTextColor = a.getColor(R.styleable.MessageListView_streamDateSeparatorDateTextColor, getColor(R.color.stream_gray_dark));
        dateSeparatorDateTextFontPath = a.getString(R.styleable.MessageListView_streamDateSeparatorDateTextFontPath);
        dateSeparatorDateTextStyle = a.getInt(R.styleable.MessageListView_streamDateSeparatorDateTextStyle, Typeface.BOLD);

        dateSeparatorLineWidth = a.getDimensionPixelSize(R.styleable.MessageListView_streamDateSeparatorLineWidth, getDimension(R.dimen.stream_date_separator_line_width));
        dateSeparatorLineColor = a.getColor(R.styleable.MessageListView_streamDateSeparatorLineColor, getColor(R.color.stream_gray_dark));
        dateSeparatorLineDrawable = a.getResourceId(R.styleable.MessageListView_streamDateSeparatorLineDrawable, -1);

        userNameShow = a.getBoolean(R.styleable.MessageListView_streamUserNameShow, true);
        messageDateShow = a.getBoolean(R.styleable.MessageListView_streamMessageDateShow, true);

        a.recycle();
    }

    public int getMessageTextSize(boolean isMine) {
        return isMine ? messageTextSizeMine : messageTextSizeTheirs;
    }

    public int getMessageTextColor(boolean isMine) {
        return isMine ? messageTextColorMine : messageTextColorTheirs;
    }

    public int getMessageTextStyle(boolean isMine) {
        return isMine ? messageTextStyleMine : messageTextStyleTheirs;
    }


    public String getMessageTextFontPath(boolean isMine) {
        return isMine ? messageTextFontPathMine : messageTextFontPathTheirs;
    }

    public int getMessageBubbleDrawableMine() {
        return messageBubbleDrawableMine;
    }

    public int getMessageBubbleDrawableTheirs() {
        return messageBubbleDrawableTheirs;
    }

    public int getMessageTopLeftCornerRadius(boolean isMine) {
        return isMine ? messageTopLeftCornerRadiusMine : messageTopLeftCornerRadiusTheirs;
    }

    public int getMessageTopRightCornerRadius(boolean isMine) {
        return isMine ? messageTopRightCornerRadiusMine : messageTopRightCornerRadiusTheirs;
    }

    public int getMessageBottomRightCornerRadius(boolean isMine) {
        return isMine ? messageBottomRightCornerRadiusMine : messageBottomRightCornerRadiusTheirs;
    }

    public int getMessageBottomLeftCornerRadius(boolean isMine) {
        return isMine ? messageBottomLeftCornerRadiusMine : messageBottomLeftCornerRadiusTheirs;
    }

    public int getMessageBackgroundColor(boolean isMine) {
        return isMine ? messageBackgroundColorMine : messageBackgroundColorTheirs;
    }


    public int getMessageBorderColor(boolean isMine) {
        return isMine ? messageBorderColorMine : messageBorderColorTheirs;
    }

    public int getMessageBorderWidth(boolean isMine) {
        return isMine ? messageBorderWidthMine : messageBorderWidthTheirs;
    }

    // Attachment
    public int getAttachmentTitleTextSize(boolean isMine) {
        return isMine ? attachmentTitleTextSizeMine : attachmentTitleTextSizeTheirs;
    }

    public int getAttachmentTitleTextColor(boolean isMine) {
        return isMine ? attachmentTitleTextColorMine : attachmentTitleTextColorTheirs;
    }

    public String getAttachmentTitleTextFontPath() {
        return attachmentTitleTextFontPath;
    }

    public int getAttachmentTitleTextStyle(boolean isMine) {
        return isMine ? attachmentTitleTextStyleMine : attachmentTitleTextStyleTheirs;
    }

    public int getAttachmentDescriptionTextSize(boolean isMine) {
        return isMine ? attachmentDescriptionTextSizeMine : attachmentDescriptionTextSizeTheirs;
    }

    public int getAttachmentDescriptionTextColor(boolean isMine) {
        return isMine ? attachmentDescriptionTextColorMine : attachmentDescriptionTextColorTheirs;
    }

    public String getAttachmentDescriptionTextFontPath() {
        return attachmentDescriptionTextFontPath;
    }

    public int getAttachmentDescriptionTextStyle(boolean isMine) {
        return isMine ? attachmentDescriptionTextStyleMine : attachmentDescriptionTextStyleTheirs;
    }

    public int getAttachmentFileSizeTextSize(boolean isMine) {
        return isMine ? attachmentFileSizeTextSizeMine : attachmentFileSizeTextSizeTheirs;
    }

    public int getAttachmentFileSizeTextColor(boolean isMine) {
        return isMine ? attachmentFileSizeTextColorMine : attachmentFileSizeTextColorTheirs;
    }

    public String getAttachmentFileSizeTextFontPath() {
        return attachmentFileSizeTextFontPath;
    }

    public int getAttachmentFileSizeTextStyle(boolean isMine) {
        return isMine ? attachmentFileSizeTextStyleMine : attachmentFileSizeTextStyleTheirs;
    }

    // Reaction Dialog
    public boolean isReactionEnabled() {
        return reactionEnabled;
    }

    public int getReactionViewBgDrawable() {
        return reactionViewBgDrawable;
    }

    public int getReactionViewBgColor() {
        return reactionViewBgColor;
    }

    public int getReactionViewEmojiSize() {
        return reactionViewEmojiSize;
    }

    public int getReactionViewEmojiMargin() {
        return reactionViewEmojiMargin;
    }

    public int getReactionInputBgColor() {
        return reactionInputBgColor;
    }

    public int getReactionInputEmojiSize() {
        return reactionInputEmojiSize;
    }

    public int getReactionInputEmojiMargin() {
        return reactionInputEmojiMargin;
    }


    public boolean isThreadEnabled() {
        return threadEnabled;
    }


    // Date Separator

    public int getDateSeparatorDateTextSize() {
        return dateSeparatorDateTextSize;
    }

    public String getDateSeparatorDateTextFontPath() {
        return dateSeparatorDateTextFontPath;
    }

    public int getDateSeparatorDateTextStyle() {
        return dateSeparatorDateTextStyle;
    }

    public int getDateSeparatorDateTextColor() {
        return dateSeparatorDateTextColor;
    }

    public int getDateSeparatorLineColor() {
        return dateSeparatorLineColor;
    }

    public int getDateSeparatorLineWidth() {
        return dateSeparatorLineWidth;
    }

    public int getDateSeparatorLineDrawable() {
        return dateSeparatorLineDrawable;
    }

    public boolean isUserNameShow() {
        return userNameShow;
    }

    public boolean isMessageDateShow() {
        return messageDateShow;
    }
}