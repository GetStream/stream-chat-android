package io.getstream.chat.android.ui.messages.view
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ReadStateStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat

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

    // public val avatarStyle: AvatarStyle
    public val readStateStyle: ReadStateStyle

    // MessageMoreActionDialog
    public val startThreadMessageActionEnabled: Boolean
    public val copyMessageActionEnabled: Boolean
    public val flagMessageActionEnabled: Boolean
    public val deleteMessageActionEnabled: Boolean
    public val editMessageActionEnabled: Boolean

    // Scroll Button
    public val scrollButtonViewStyle: ScrollButtonViewStyle

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
                R.styleable.MessageListView_streamUiMessageTextSizeMine,
                res.getDimensionPixelSize(R.dimen.stream_message_text_font_size)
            )
            .color(R.styleable.MessageListView_streamUiMessageTextColorMine, Color.BLACK)
            .font(
                R.styleable.MessageListView_streamUiMessageTextFontMineAssets,
                R.styleable.MessageListView_streamUiMessageTextFontMine
            )
            .style(R.styleable.MessageListView_streamUiMessageTextStyleMine, Typeface.NORMAL)
            .build()
        messageTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiMessageTextSizeTheirs,
                res.getDimensionPixelSize(R.dimen.stream_message_text_font_size)
            )
            .color(R.styleable.MessageListView_streamUiMessageTextColorTheirs, Color.BLACK)
            .font(
                R.styleable.MessageListView_streamUiMessageTextFontTheirsAssets,
                R.styleable.MessageListView_streamUiMessageTextFontTheirs
            )
            .style(R.styleable.MessageListView_streamUiMessageTextStyleTheirs, Typeface.NORMAL)
            .build()

        // Message Bubble
        messageBubbleDrawableMine =
            a.getResourceId(R.styleable.MessageListView_streamUiMessageBubbleDrawableMine, -1)
        messageBubbleDrawableTheirs =
            a.getResourceId(R.styleable.MessageListView_streamUiMessageBubbleDrawableTheirs, -1)
        messageTopLeftCornerRadiusMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageTopLeftCornerRadiusMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageTopRightCornerRadiusMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageTopRightCornerRadiusMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageBottomRightCornerRadiusMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageBottomRightCornerRadiusMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius2
            )
        )
        messageBottomLeftCornerRadiusMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageBottomLeftCornerRadiusMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageTopLeftCornerRadiusTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageTopLeftCornerRadiusTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageTopRightCornerRadiusTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageTopRightCornerRadiusTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageBottomRightCornerRadiusTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageBottomRightCornerRadiusTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius1
            )
        )
        messageBottomLeftCornerRadiusTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageBottomLeftCornerRadiusTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_corner_radius2
            )
        )
        messageBackgroundColorMine = a.getColor(
            R.styleable.MessageListView_streamUiMessageBackgroundColorMine,
            ContextCompat.getColor(c, R.color.stream_message_background_outgoing)
        )
        messageBackgroundColorTheirs = a.getColor(
            R.styleable.MessageListView_streamUiMessageBackgroundColorTheirs,
            ContextCompat.getColor(c, R.color.stream_message_background_incoming)
        )
        messageBorderColorMine = a.getColor(
            R.styleable.MessageListView_streamUiMessageBorderColorMine,
            ContextCompat.getColor(c, R.color.stream_message_stroke)
        )
        messageBorderColorTheirs = a.getColor(
            R.styleable.MessageListView_streamUiMessageBorderColorTheirs,
            ContextCompat.getColor(c, R.color.stream_message_stroke)
        )
        messageBorderWidthMine = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageBorderWidthMine,
            res.getDimensionPixelSize(
                R.dimen.stream_message_stroke
            )
        )
        messageBorderWidthTheirs = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiMessageBorderWidthTheirs,
            res.getDimensionPixelSize(
                R.dimen.stream_message_stroke
            )
        )
        messageLinkTextColorMine =
            a.getColor(R.styleable.MessageListView_streamUiMessageLinkTextColorMine, 0)
        messageLinkTextColorTheirs =
            a.getColor(R.styleable.MessageListView_streamUiMessageLinkTextColorTheirs, 0)
        messageUserNameText = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiMessageUserNameTextSize,
                res.getDimensionPixelSize(R.dimen.stream_attach_description_text)
            )
            .color(
                R.styleable.MessageListView_streamUiMessageUserNameTextColor,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamUiMessageUserNameTextFontAssets,
                R.styleable.MessageListView_streamUiMessageUserNameTextFont
            )
            .style(R.styleable.MessageListView_streamUiMessageUserNameTextStyle, Typeface.BOLD)
            .build()
        messageDateTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiMessageDateTextSizeMine,
                res.getDimensionPixelSize(R.dimen.stream_attach_description_text)
            )
            .color(
                R.styleable.MessageListView_streamUiMessageDateTextColorMine,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamUiMessageDateTextFontAssetsMine,
                R.styleable.MessageListView_streamUiMessageDateTextFontMine
            )
            .style(R.styleable.MessageListView_streamUiMessageDateTextStyleMine, Typeface.NORMAL)
            .build()
        messageDateTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiMessageDateTextSizeTheirs,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_description_text
                )
            )
            .color(
                R.styleable.MessageListView_streamUiMessageDateTextColorTheirs,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamUiMessageDateTextFontAssetsTheirs,
                R.styleable.MessageListView_streamUiMessageDateTextFontTheirs
            )
            .style(R.styleable.MessageListView_streamUiMessageDateTextStyleTheirs, Typeface.NORMAL)
            .build()

        // Attachment
        attachmentTitleTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiAttachmentTitleTextSizeMine,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_title_text
                )
            )
            .color(
                R.styleable.MessageListView_streamUiAttachmentTitleTextColorMine,
                ContextCompat.getColor(c, R.color.stream_attach_title_text)
            )
            .font(
                R.styleable.MessageListView_streamUiAttachmentTitleTextFontAssetsMine,
                R.styleable.MessageListView_streamUiAttachmentTitleTextFontMine
            )
            .style(R.styleable.MessageListView_streamUiAttachmentTitleTextStyleMine, Typeface.BOLD)
            .build()
        attachmentTitleTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiAttachmentTitleTextSizeTheirs,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_title_text
                )
            )
            .color(
                R.styleable.MessageListView_streamUiAttachmentTitleTextColorTheirs,
                ContextCompat.getColor(c, R.color.stream_attach_title_text)
            )
            .font(
                R.styleable.MessageListView_streamUiAttachmentTitleTextFontAssetsTheirs,
                R.styleable.MessageListView_streamUiAttachmentTitleTextFontTheirs
            )
            .style(R.styleable.MessageListView_streamUiAttachmentTitleTextStyleTheirs, Typeface.BOLD)
            .build()
        attachmentBackgroundColorMine = a.getColor(
            R.styleable.MessageListView_streamUiAttachmentBackgroundColorMine,
            messageBackgroundColorMine
        )
        attachmentBackgroundColorTheirs = a.getColor(
            R.styleable.MessageListView_streamUiAttachmentBackgroundColorTheirs,
            messageBackgroundColorTheirs
        )
        attachmentBorderColorMine = a.getColor(
            R.styleable.MessageListView_streamUiAttachmentBorderColorMine,
            messageBorderColorMine
        )
        attachmentBorderColorTheirs = a.getColor(
            R.styleable.MessageListView_streamUiAttachmentBorderColorTheirs,
            messageBorderColorTheirs
        )
        attachmentDescriptionTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextSizeMine,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_description_text
                )
            )
            .color(
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextColorMine,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextFontAssetsMine,
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextFontMine
            )
            .style(
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextStyleMine,
                Typeface.NORMAL
            )
            .build()
        attachmentDescriptionTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextSizeTheirs,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_description_text
                )
            )
            .color(
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextColorTheirs,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextFontAssetsTheirs,
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextFontTheirs
            )
            .style(
                R.styleable.MessageListView_streamUiAttachmentDescriptionTextStyleTheirs,
                Typeface.NORMAL
            )
            .build()
        attachmentFileSizeTextMine = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextSizeMine,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_file_size_text
                )
            )
            .color(
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextColorMine,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextFontAssetsMine,
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextFontMine
            )
            .style(R.styleable.MessageListView_streamUiAttachmentFileSizeTextStyleMine, Typeface.BOLD)
            .build()
        attachmentFileSizeTextTheirs = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextSizeTheirs,
                res.getDimensionPixelSize(
                    R.dimen.stream_attach_file_size_text
                )
            )
            .color(
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextColorTheirs,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextFontAssetsTheirs,
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextFontTheirs
            )
            .style(
                R.styleable.MessageListView_streamUiAttachmentFileSizeTextStyleTheirs,
                Typeface.BOLD
            )
            .build()
        attachmentPreviewMaxLines = a.getInt(
            R.styleable.MessageListView_streamUiAttachmentPreviewMaxLines,
            res.getInteger(
                R.integer.stream_attachment_preview_max_lines
            )
        )
        require(attachmentPreviewMaxLines > 0) { "streamAttachmentPreviewMaxLines value must be greater than 0" }

        // Reaction
        isReactionEnabled = a.getBoolean(R.styleable.MessageListView_streamUiReactionEnabled, true)
        reactionViewBgDrawable =
            a.getResourceId(R.styleable.MessageListView_streamUiReactionViewBgDrawable, -1)
        reactionViewBgColor = a.getColor(
            R.styleable.MessageListView_streamUiReactionViewBgColor,
            ContextCompat.getColor(c, R.color.stream_reaction_input_background)
        )
        reactionViewEmojiSize = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiReactionViewEmojiSize,
            res.getDimensionPixelSize(
                R.dimen.stream_reaction_view_emoji_size
            )
        )
        reactionViewEmojiMargin = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiReactionViewEmojiMargin,
            res.getDimensionPixelSize(
                R.dimen.stream_reaction_view_emoji_margin
            )
        )
        // Reaction Dialog
        reactionInputBgColor = a.getColor(
            R.styleable.MessageListView_streamUiReactionInputbgColor,
            ContextCompat.getColor(c, R.color.stream_reaction_input_background)
        )
        reactionInputEmojiSize = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiReactionInputEmojiSize,
            res.getDimensionPixelSize(
                R.dimen.stream_reaction_input_emoji_size
            )
        )
        reactionInputEmojiMargin = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiReactionInputEmojiMargin,
            res.getDimensionPixelSize(
                R.dimen.stream_reaction_input_emoji_margin
            )
        )

        // Read State
        readStateStyle = ReadStateStyle.Builder(a, c)
            .isReadStateEnabled(R.styleable.MessageListView_streamUiShowReadState, true)
            .readStateAvatarWidth(
                R.styleable.MessageListView_streamUiReadStateAvatarWidth,
                res.getDimensionPixelSize(R.dimen.stream_read_state_avatar_width)
            )
            .readStateAvatarHeight(
                R.styleable.MessageListView_streamUiReadStateAvatarHeight,
                res.getDimensionPixelSize(R.dimen.stream_read_state_avatar_height)
            )
            .readStateText(
                textSize = R.styleable.MessageListView_streamUiReadStateTextSize,
                defaultTextSize = R.dimen.stream_read_state_text_size,
                textColor = R.styleable.MessageListView_streamUiReadStateTextColor,
                defaultTextColor = Color.BLACK,
                textFontAssetsStyleableId = R.styleable.MessageListView_streamUiReadStateTextFontAssets,
                textFontStyleableId = R.styleable.MessageListView_streamUiReadStateTextFont,
                textStyleStyleableId = R.styleable.MessageListView_streamUiReadStateTextStyle,
            )
            .isDeliveredIndicatorEnabled(R.styleable.MessageListView_streamUiShowDeliveredState, true)
            .build()

        isThreadEnabled = a.getBoolean(R.styleable.MessageListView_streamUiThreadEnabled, true)
        dateSeparatorDateText = TextStyle.Builder(a)
            .size(
                R.styleable.MessageListView_streamUiDateSeparatorDateTextSize,
                res.getDimensionPixelSize(
                    R.dimen.stream_date_separator_text
                )
            )
            .color(
                R.styleable.MessageListView_streamUiDateSeparatorDateTextColor,
                ContextCompat.getColor(c, R.color.stream_gray_dark)
            )
            .font(
                R.styleable.MessageListView_streamUiDateSeparatorDateTextFontAssets,
                R.styleable.MessageListView_streamUiDateSeparatorDateTextFont
            )
            .style(R.styleable.MessageListView_streamUiDateSeparatorDateTextStyle, Typeface.BOLD)
            .build()
        dateSeparatorLineWidth = a.getDimensionPixelSize(
            R.styleable.MessageListView_streamUiDateSeparatorLineWidth,
            res.getDimensionPixelSize(
                R.dimen.stream_date_separator_line_width
            )
        )
        dateSeparatorLineColor = a.getColor(
            R.styleable.MessageListView_streamUiDateSeparatorLineColor,
            ContextCompat.getColor(c, R.color.stream_gray_dark)
        )
        dateSeparatorLineDrawable =
            a.getResourceId(R.styleable.MessageListView_streamUiDateSeparatorLineDrawable, -1)
        isUserNameShow = a.getBoolean(R.styleable.MessageListView_streamUiUserNameShow, true)
        isMessageDateShow = a.getBoolean(R.styleable.MessageListView_streamUiMessageDateShow, true)

        startThreadMessageActionEnabled = a.getBoolean(
            R.styleable.MessageListView_streamUiStartThreadMessageActionEnabled,
            true
        )
        copyMessageActionEnabled = a.getBoolean(R.styleable.MessageListView_streamUiCopyMessageActionEnabled, true)
        flagMessageActionEnabled = a.getBoolean(R.styleable.MessageListView_streamUiFlagMessageActionEnabled, true)
        deleteMessageActionEnabled = a.getBoolean(R.styleable.MessageListView_streamUiDeleteMessageActionEnabled, true)
        editMessageActionEnabled = a.getBoolean(R.styleable.MessageListView_streamUiEditMessageActionEnabled, true)

        scrollButtonViewStyle = ScrollButtonViewStyle.Builder(a)
            .scrollButtonEnabled(
                R.styleable.MessageListView_streamUiScrollButtonEnabled,
                true
            )
            .scrollButtonUnreadEnabled(
                R.styleable.MessageListView_streamUiScrollButtonUnreadEnabled,
                true
            )
            .scrollButtonColor(
                R.styleable.MessageListView_streamUiScrollButtonColor,
                c.getColorCompat(R.color.stream_white)
            )
            .scrollButtonRippleColor(
                R.styleable.MessageListView_streamUiScrollButtonRippleColor,
                c.getColorCompat(R.color.stream_ui_grey_light)
            )
            .scrollButtonBadgeColor(
                R.styleable.MessageListView_streamUiScrollButtonBadgeColor,
                c.getColorCompat(R.color.stream_ui_blue)
            )
            .scrollButtonIcon(
                R.styleable.MessageListView_streamUiScrollButtonIcon,
                c.getDrawable(R.drawable.stream_ui_ic_down)
            ).build()

        a.recycle()
    }
}
