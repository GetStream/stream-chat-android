package com.getstream.sdk.chat.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.style.TextStyle
import com.getstream.sdk.chat.view.messages.AvatarStyle

public class MessageListViewStyle(c: Context, attrs: AttributeSet?) {
    // Message text
    public var messageTextMine: TextStyle
    public var messageTextTheirs: TextStyle

    // Message Bubble
    private val messageBubbleDrawableMine: Int
    private val messageBubbleDrawableTheirs: Int
    private val messageTopLeftCornerRadiusMine: Int
    private val messageTopRightCornerRadiusMine: Int
    private val messageBottomRightCornerRadiusMine: Int
    private val messageBottomLeftCornerRadiusMine: Int
    private val messageTopLeftCornerRadiusTheirs: Int
    private val messageTopRightCornerRadiusTheirs: Int
    private val messageBottomRightCornerRadiusTheirs: Int
    private val messageBottomLeftCornerRadiusTheirs: Int
    private val messageBackgroundColorMine: Int
    private val messageBackgroundColorTheirs: Int
    private val messageBorderColorMine: Int
    private val messageBorderColorTheirs: Int
    private val messageBorderWidthMine: Int
    private val messageBorderWidthTheirs: Int
    private val messageLinkTextColorMine: Int
    private val messageLinkTextColorTheirs: Int
    public var messageUserNameText: TextStyle
    public var messageDateTextMine: TextStyle
    public var messageDateTextTheirs: TextStyle

    // Attachment
    public var attachmentTitleTextMine: TextStyle
    public var attachmentTitleTextTheirs: TextStyle
    public var attachmentDescriptionTextMine: TextStyle
    public var attachmentDescriptionTextTheirs: TextStyle
    public var attachmentFileSizeTextMine: TextStyle
    public var attachmentFileSizeTextTheirs: TextStyle
    private val attachmentBackgroundColorMine: Int
    private val attachmentBackgroundColorTheirs: Int
    private val attachmentBorderColorMine: Int
    private val attachmentBorderColorTheirs: Int
    public val attachmentPreviewMaxLines: Int

    // Reaction Dialog
    // Reaction
    public val isReactionEnabled: Boolean

    // ReactionView
    public val reactionViewBgDrawable: Int
    public val reactionViewBgColor: Int
    public val reactionViewEmojiSize: Int
    public val reactionViewEmojiMargin: Int

    // ReactionInput
    public val reactionInputBgColor: Int
    public val reactionInputEmojiSize: Int
    public val reactionInputEmojiMargin: Int
    public val isThreadEnabled: Boolean
    public val isUserNameShow: Boolean
    public val isMessageDateShow: Boolean

    // Date Separator
    public var dateSeparatorDateText: TextStyle

    // Date Separator
    public val dateSeparatorLineColor: Int
    public val dateSeparatorLineWidth: Int
    public val dateSeparatorLineDrawable: Int

    public val avatarStyle: AvatarStyle
    public val readStateStyle: ReadStateStyle

    // MessageMoreActionDialog
    public val messageActionButtonsBackground: Drawable?
    public val messageActionButtonsIconTint: ColorStateList?
    public val messageActionButtonsTextStyle: TextStyle
    public val startThreadMessageActionEnabled: Boolean
    public val copyMessageActionEnabled: Boolean
    public val flagMessageActionEnabled: Boolean
    public val deleteMessageActionEnabled: Boolean
    public val editMessageActionEnabled: Boolean

    public fun getMessageBubbleDrawable(isMine: Boolean): Int {
        return if (isMine) messageBubbleDrawableMine else messageBubbleDrawableTheirs
    }

    public fun getMessageTopLeftCornerRadius(isMine: Boolean): Int {
        return if (isMine) messageTopLeftCornerRadiusMine else messageTopLeftCornerRadiusTheirs
    }

    public fun getMessageTopRightCornerRadius(isMine: Boolean): Int {
        return if (isMine) messageTopRightCornerRadiusMine else messageTopRightCornerRadiusTheirs
    }

    public fun getMessageBottomRightCornerRadius(isMine: Boolean): Int {
        return if (isMine) messageBottomRightCornerRadiusMine else messageBottomRightCornerRadiusTheirs
    }

    public fun getMessageBottomLeftCornerRadius(isMine: Boolean): Int {
        return if (isMine) messageBottomLeftCornerRadiusMine else messageBottomLeftCornerRadiusTheirs
    }

    public fun getMessageBackgroundColor(isMine: Boolean): Int {
        return if (isMine) messageBackgroundColorMine else messageBackgroundColorTheirs
    }

    public fun getMessageBorderColor(isMine: Boolean): Int {
        return if (isMine) messageBorderColorMine else messageBorderColorTheirs
    }

    public fun getMessageBorderWidth(isMine: Boolean): Int {
        return if (isMine) messageBorderWidthMine else messageBorderWidthTheirs
    }

    public fun getMessageLinkTextColor(isMine: Boolean): Int {
        return if (isMine) messageLinkTextColorMine else messageLinkTextColorTheirs
    }

    // Attachment
    public fun getAttachmentBackgroundColor(isMine: Boolean): Int {
        return if (isMine) attachmentBackgroundColorMine else attachmentBackgroundColorTheirs
    }

    public fun getAttachmentBorderColor(isMine: Boolean): Int {
        return if (isMine) attachmentBorderColorMine else attachmentBorderColorTheirs
    }

    init {
        val res = c.resources

        // parse the attributes
        val a = c.obtainStyledAttributes(
            attrs,
            R.styleable.MessageListView,
            0,
            0
        )

        // Message Text
        messageTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamMessageTextSizeMine,
                res.getDimensionPixelSize(R.dimen.stream_message_text_font_size)
            )
            .color(R.styleable.MessageListView_streamMessageTextColorMine, Color.BLACK)
            .font(
                R.styleable.MessageListView_streamMessageTextFontMineAssets,
                R.styleable.MessageListView_streamMessageTextFontMine
            )
            .style(R.styleable.MessageListView_streamMessageTextStyleMine, Typeface.NORMAL)
            .build()
        messageTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamMessageTextSizeTheirs,
                res.getDimensionPixelSize(R.dimen.stream_message_text_font_size)
            )
            .color(R.styleable.MessageListView_streamMessageTextColorTheirs, Color.BLACK)
            .font(
                R.styleable.MessageListView_streamMessageTextFontTheirsAssets,
                R.styleable.MessageListView_streamMessageTextFontTheirs
            )
            .style(R.styleable.MessageListView_streamMessageTextStyleTheirs, Typeface.NORMAL)
            .build()

        // Message Bubble
        messageBubbleDrawableMine =
            a.getResourceId(R.styleable.MessageListView_streamMessageBubbleDrawableMine, -1)
        messageBubbleDrawableTheirs =
            a.getResourceId(R.styleable.MessageListView_streamMessageBubbleDrawableTheirs, -1)
        messageTopLeftCornerRadiusMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageTopLeftCornerRadiusMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageTopRightCornerRadiusMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageTopRightCornerRadiusMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageBottomRightCornerRadiusMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageBottomRightCornerRadiusMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius2
            )
        )
        messageBottomLeftCornerRadiusMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageBottomLeftCornerRadiusMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageTopLeftCornerRadiusTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageTopLeftCornerRadiusTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageTopRightCornerRadiusTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageTopRightCornerRadiusTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageBottomRightCornerRadiusTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageBottomRightCornerRadiusTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageBottomLeftCornerRadiusTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageBottomLeftCornerRadiusTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius2
            )
        )
        messageBackgroundColorMine = a.getColor(
            R.styleable.MessageListView_streamMessageBackgroundColorMine,
            ContextCompat.getColor(c, R.color.stream_message_background_outgoing)
        )
        messageBackgroundColorTheirs = a.getColor(
            R.styleable.MessageListView_streamMessageBackgroundColorTheirs,
            ContextCompat.getColor(c, R.color.stream_message_background_incoming)
        )
        messageBorderColorMine = a.getColor(
            R.styleable.MessageListView_streamMessageBorderColorMine,
            ContextCompat.getColor(c, R.color.stream_message_stroke)
        )
        messageBorderColorTheirs = a.getColor(
            R.styleable.MessageListView_streamMessageBorderColorTheirs,
            ContextCompat.getColor(c, R.color.stream_message_stroke)
        )
        messageBorderWidthMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageBorderWidthMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_stroke
            )
        )
        messageBorderWidthTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamMessageBorderWidthTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_stroke
            )
        )
        messageLinkTextColorMine =
            a.getColor(R.styleable.MessageListView_streamMessageLinkTextColorMine, 0)
        messageLinkTextColorTheirs =
            a.getColor(R.styleable.MessageListView_streamMessageLinkTextColorTheirs, 0)
        messageUserNameText = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamMessageUserNameTextSize,
                res.getDimensionPixelSize(R.dimen.stream_attach_description_text)
            )
            .color(
                R.styleable.MessageListView_streamMessageUserNameTextColor,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamMessageUserNameTextFontAssets,
                R.styleable.MessageListView_streamMessageUserNameTextFont
            )
            .style(R.styleable.MessageListView_streamMessageUserNameTextStyle, Typeface.BOLD)
            .build()
        messageDateTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamMessageDateTextSizeMine,
                res.getDimensionPixelSize(R.dimen.stream_attach_description_text)
            )
            .color(
                R.styleable.MessageListView_streamMessageDateTextColorMine,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamMessageDateTextFontAssetsMine,
                R.styleable.MessageListView_streamMessageDateTextFontMine
            )
            .style(R.styleable.MessageListView_streamMessageDateTextStyleMine, Typeface.NORMAL)
            .build()
        messageDateTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamMessageDateTextSizeTheirs,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_description_text
                )
            )
            .color(
                R.styleable.MessageListView_streamMessageDateTextColorTheirs,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamMessageDateTextFontAssetsTheirs,
                R.styleable.MessageListView_streamMessageDateTextFontTheirs
            )
            .style(R.styleable.MessageListView_streamMessageDateTextStyleTheirs, Typeface.NORMAL)
            .build()

        // Attachment
        attachmentTitleTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamAttachmentTitleTextSizeMine,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_title_text
                )
            )
            .color(
                R.styleable.MessageListView_streamAttachmentTitleTextColorMine,
                ContextCompat.getColor(c, R.color.stream_attach_title_text)
            )
            .font(
                R.styleable.MessageListView_streamAttachmentTitleTextFontAssetsMine,
                R.styleable.MessageListView_streamAttachmentTitleTextFontMine
            )
            .style(R.styleable.MessageListView_streamAttachmentTitleTextStyleMine, Typeface.BOLD)
            .build()
        attachmentTitleTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamAttachmentTitleTextSizeTheirs,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_title_text
                )
            )
            .color(
                R.styleable.MessageListView_streamAttachmentTitleTextColorTheirs,
                ContextCompat.getColor(c, R.color.stream_attach_title_text)
            )
            .font(
                R.styleable.MessageListView_streamAttachmentTitleTextFontAssetsTheirs,
                R.styleable.MessageListView_streamAttachmentTitleTextFontTheirs
            )
            .style(R.styleable.MessageListView_streamAttachmentTitleTextStyleTheirs, Typeface.BOLD)
            .build()
        attachmentBackgroundColorMine = a.getColor(
            R.styleable.MessageListView_streamAttachmentBackgroundColorMine,
            messageBackgroundColorMine
        )
        attachmentBackgroundColorTheirs = a.getColor(
            R.styleable.MessageListView_streamAttachmentBackgroundColorTheirs,
            messageBackgroundColorTheirs
        )
        attachmentBorderColorMine = a.getColor(
            R.styleable.MessageListView_streamAttachmentBorderColorMine,
            messageBorderColorMine
        )
        attachmentBorderColorTheirs = a.getColor(
            R.styleable.MessageListView_streamAttachmentBorderColorTheirs,
            messageBorderColorTheirs
        )
        attachmentDescriptionTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamAttachmentDescriptionTextSizeMine,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_description_text
                )
            )
            .color(
                R.styleable.MessageListView_streamAttachmentDescriptionTextColorMine,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamAttachmentDescriptionTextFontAssetsMine,
                R.styleable.MessageListView_streamAttachmentDescriptionTextFontMine
            )
            .style(
                R.styleable.MessageListView_streamAttachmentDescriptionTextStyleMine,
                Typeface.NORMAL
            )
            .build()
        attachmentDescriptionTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamAttachmentDescriptionTextSizeTheirs,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_description_text
                )
            )
            .color(
                R.styleable.MessageListView_streamAttachmentDescriptionTextColorTheirs,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamAttachmentDescriptionTextFontAssetsTheirs,
                R.styleable.MessageListView_streamAttachmentDescriptionTextFontTheirs
            )
            .style(
                R.styleable.MessageListView_streamAttachmentDescriptionTextStyleTheirs,
                Typeface.NORMAL
            )
            .build()
        attachmentFileSizeTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamAttachmentFileSizeTextSizeMine,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_file_size_text
                )
            )
            .color(
                R.styleable.MessageListView_streamAttachmentFileSizeTextColorMine,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamAttachmentFileSizeTextFontAssetsMine,
                R.styleable.MessageListView_streamAttachmentFileSizeTextFontMine
            )
            .style(R.styleable.MessageListView_streamAttachmentFileSizeTextStyleMine, Typeface.BOLD)
            .build()
        attachmentFileSizeTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamAttachmentFileSizeTextSizeTheirs,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_file_size_text
                )
            )
            .color(
                R.styleable.MessageListView_streamAttachmentFileSizeTextColorTheirs,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamAttachmentFileSizeTextFontAssetsTheirs,
                R.styleable.MessageListView_streamAttachmentFileSizeTextFontTheirs
            )
            .style(
                R.styleable.MessageListView_streamAttachmentFileSizeTextStyleTheirs,
                Typeface.BOLD
            )
            .build()
        attachmentPreviewMaxLines = a.getInt(
            R.styleable.MessageListView_streamAttachmentPreviewMaxLines,
            res.getInteger(
                R.integer.stream_attachment_preview_max_lines
            )
        )
        require(attachmentPreviewMaxLines > 0) { "streamAttachmentPreviewMaxLines value must be greater than 0" }

        // Reaction
        isReactionEnabled = a.getBoolean(R.styleable.MessageListView_streamReactionEnabled, true)
        reactionViewBgDrawable =
            a.getResourceId(R.styleable.MessageListView_streamReactionViewBgDrawable, -1)
        reactionViewBgColor = a.getColor(
            R.styleable.MessageListView_streamReactionViewBgColor,
            ContextCompat.getColor(c, R.color.stream_reaction_input_background)
        )
        reactionViewEmojiSize = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamReactionViewEmojiSize,
            res.getDimensionPixelSize(
                R.dimen.stream_reaction_view_emoji_size
            )
        )
        reactionViewEmojiMargin = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamReactionViewEmojiMargin,
            res.getDimensionPixelSize(
                R.dimen.stream_reaction_view_emoji_margin
            )
        )
        // Reaction Dialog
        reactionInputBgColor = a.getColor(
            R.styleable.MessageListView_streamReactionInputbgColor,
            ContextCompat.getColor(c, R.color.stream_reaction_input_background)
        )
        reactionInputEmojiSize = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamReactionInputEmojiSize,
            res.getDimensionPixelSize(
                R.dimen.stream_reaction_input_emoji_size
            )
        )
        reactionInputEmojiMargin = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamReactionInputEmojiMargin,
            res.getDimensionPixelSize(
                R.dimen.stream_reaction_input_emoji_margin
            )
        )

        // Avatar
        avatarStyle = AvatarStyle.Builder(a, c)
            .avatarWidth(
                R.styleable.MessageListView_streamAvatarWidth,
                R.dimen.stream_message_avatar_width
            )
            .avatarHeight(
                R.styleable.MessageListView_streamAvatarHeight,
                R.dimen.stream_message_avatar_height
            )
            .avatarBorderWidth(
                R.styleable.MessageListView_streamAvatarBorderWidth,
                R.dimen.stream_channel_avatar_border_width
            )
            .avatarBorderColor(R.styleable.MessageListView_streamAvatarBorderColor, Color.WHITE)
            .avatarBackgroundColor(
                R.styleable.MessageListView_streamAvatarBackGroundColor,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .avatarInitialText(
                avatarTextSizeStyleableId = R.styleable.MessageListView_streamAvatarTextSize,
                avatarTextSizeDefaultValue = R.dimen.stream_channel_initials,
                avatarTextColorStyleableId = R.styleable.MessageListView_streamAvatarTextColor,
                avatarTextColorDefaultValue = Color.WHITE,
                avatarTextFontAssetsStyleableId = R.styleable.MessageListView_streamAvatarTextFontAssets,
                avatarTextFontStyleableId = R.styleable.MessageListView_streamAvatarTextFont,
                avatarTextStyleStyleableId = R.styleable.MessageListView_streamAvatarTextStyle
            )
            .build()

        // Read State
        readStateStyle = ReadStateStyle.Builder(a, c)
            .isReadStateEnabled(R.styleable.MessageListView_streamShowReadState, true)
            .readStateAvatarWidth(
                R.styleable.MessageListView_streamReadStateAvatarWidth,
                res.getDimensionPixelSize(R.dimen.stream_read_state_avatar_width)
            )
            .readStateAvatarHeight(
                R.styleable.MessageListView_streamReadStateAvatarHeight,
                res.getDimensionPixelSize(R.dimen.stream_read_state_avatar_height)
            )
            .readStateText(
                textSize = R.styleable.MessageListView_streamReadStateTextSize,
                defaultTextSize = R.dimen.stream_read_state_text_size,
                textColor = R.styleable.MessageListView_streamReadStateTextColor,
                defaultTextColor = Color.BLACK,
                textFontAssetsStyleableId = R.styleable.MessageListView_streamReadStateTextFontAssets,
                textFontStyleableId = R.styleable.MessageListView_streamReadStateTextFont,
                textStyleStyleableId = R.styleable.MessageListView_streamReadStateTextStyle,
            )
            .isDeliveredIndicatorEnabled(R.styleable.MessageListView_streamShowDeliveredState, true)
            .build()

        isThreadEnabled = a.getBoolean(R.styleable.MessageListView_streamThreadEnabled, true)
        dateSeparatorDateText = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamDateSeparatorDateTextSize,
                res.getDimensionPixelSize(
                    R.dimen.stream_date_separator_text
                )
            )
            .color(
                R.styleable.MessageListView_streamDateSeparatorDateTextColor,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamDateSeparatorDateTextFontAssets,
                R.styleable.MessageListView_streamDateSeparatorDateTextFont
            )
            .style(R.styleable.MessageListView_streamDateSeparatorDateTextStyle, Typeface.BOLD)
            .build()
        dateSeparatorLineWidth = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamDateSeparatorLineWidth,
            res.getDimensionPixelSize(
                R.dimen.stream_date_separator_line_width
            )
        )
        dateSeparatorLineColor = a.getColor(
            R.styleable.MessageListView_streamDateSeparatorLineColor,
            ContextCompat.getColor(c, R.color.stream_gray_dark)
        )
        dateSeparatorLineDrawable =
            a.getResourceId(R.styleable.MessageListView_streamDateSeparatorLineDrawable, -1)
        isUserNameShow = a.getBoolean(R.styleable.MessageListView_streamUserNameShow, true)
        isMessageDateShow = a.getBoolean(R.styleable.MessageListView_streamMessageDateShow, true)

        messageActionButtonsBackground = a.getDrawable(R.styleable.MessageListView_streamMessageActionButtonsBackground)
        messageActionButtonsIconTint =
            a.getColorStateList(R.styleable.MessageListView_streamMessageActionButtonsIconTint)
            ?: ContextCompat.getColorStateList(c, R.color.stream_black_54)
        messageActionButtonsTextStyle = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamMessageActionButtonsTextSize,
                res.getDimensionPixelSize(
                    R.dimen.stream_message_action_buttons_text_size
                )
            )
            .color(
                R.styleable.MessageListView_streamMessageActionButtonsTextColor,
                ContextCompat.getColor(c, R.color.stream_black_54)
            )
            .font(
                R.styleable.MessageListView_streamMessageActionButtonsTextFontAssets,
                R.styleable.MessageListView_streamMessageActionButtonsTextFont
            )
            .style(R.styleable.MessageListView_streamMessageActionButtonsTextStyle, Typeface.NORMAL)
            .build()

        startThreadMessageActionEnabled = a.getBoolean(
            R.styleable.MessageListView_streamStartThreadMessageActionEnabled,
            true
        )
        copyMessageActionEnabled = a.getBoolean(R.styleable.MessageListView_streamCopyMessageActionEnabled, true)
        flagMessageActionEnabled = a.getBoolean(R.styleable.MessageListView_streamFlagMessageActionEnabled, true)
        deleteMessageActionEnabled = a.getBoolean(R.styleable.MessageListView_streamDeleteMessageActionEnabled, true)
        editMessageActionEnabled = a.getBoolean(R.styleable.MessageListView_streamEditMessageActionEnabled, true)

        a.recycle()
    }
}
