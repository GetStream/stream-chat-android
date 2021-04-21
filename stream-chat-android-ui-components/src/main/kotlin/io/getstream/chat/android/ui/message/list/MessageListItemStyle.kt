package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.list.MessageListItemStyle.Companion.MESSAGE_STROKE_COLOR_MINE
import io.getstream.chat.android.ui.message.list.MessageListItemStyle.Companion.MESSAGE_STROKE_COLOR_THEIRS
import io.getstream.chat.android.ui.message.list.MessageListItemStyle.Companion.MESSAGE_STROKE_WIDTH_MINE
import io.getstream.chat.android.ui.message.list.MessageListItemStyle.Companion.MESSAGE_STROKE_WIDTH_THEIRS
import io.getstream.chat.android.ui.message.list.reactions.edit.EditReactionsViewStyle
import io.getstream.chat.android.ui.message.list.reactions.edit.internal.EditReactionsView
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle
import io.getstream.chat.android.ui.message.list.reactions.view.internal.ViewReactionsView
import java.io.Serializable

/**
 * Style for view holders used inside [MessageListView].
 * Use this class together with [TransformStyle.messageListItemStyleTransformer] to change styles programmatically.
 *
 * @property messageBackgroundColorMine - background color for message sent by the current user. Default - [R.color.stream_ui_grey_gainsboro]
 * @property messageBackgroundColorTheirs - background color for message sent by other user. Default - [R.color.stream_ui_white]
 * @property messageLinkTextColorMine - color for links sent by the current user. Default - [R.color.stream_ui_accent_blue]
 * @property messageLinkTextColorTheirs - color for links sent by other user. Default - [R.color.stream_ui_accent_blue]
 * @property messageLinkBackgroundColorMine - background color for message with link, sent by the current user. Default - [R.color.stream_ui_blue_alice]
 * @property messageLinkBackgroundColorTheirs - background color for message with link, sent by other user. Default - [R.color.stream_ui_blue_alice]
 * @property reactionsEnabled - enables/disables reactions feature. Enabled by default
 * @property threadsEnabled - enables/disables threads feature. Enabled by default
 * @property linkDescriptionMaxLines - max lines for link's description. Default - 5
 * @property textStyleMine - appearance for message text sent by the current user
 * @property textStyleTheirs - appearance for message text sent by other user
 * @property textStyleUserName - appearance for user name text
 * @property textStyleMessageDate - appearance for message date text
 * @property textStyleThreadCounter - appearance for thread counter text
 * @property textStyleLinkTitle - appearance for link
 * @property textStyleLinkDescription - appearance for link's description text
 * @property dateSeparatorBackgroundColor - background color for data separator. Default - [R.color.stream_ui_overlay_dark]
 * @property textStyleDateSeparator - appearance for date separator text
 * @property reactionsViewStyle - style for [ViewReactionsView]
 * @property editReactionsViewStyle - style for [EditReactionsView]
 * @property iconIndicatorSent - icon for message's sent status. Default - [R.drawable.stream_ui_ic_check_single]
 * @property iconIndicatorRead - icon for message's read status. Default - [R.drawable.stream_ui_ic_check_double]
 * @property iconIndicatorPendingSync - icon for message's pending status. Default - [R.drawable.stream_ui_ic_clock]
 * @property textStyleMessageDeleted - appearance for message deleted text
 * @property messageDeletedBackground - background color for deleted message. Default - [R.color.stream_ui_grey_whisper]
 * @property messageStrokeColorMine - stroke color for message sent by the current user. Default - [MESSAGE_STROKE_COLOR_MINE]
 * @property messageStrokeWidthMine - stroke width for message sent by the current user. Default - [MESSAGE_STROKE_WIDTH_MINE]
 * @property messageStrokeColorTheirs - stroke color for message sent by other user. Default - [MESSAGE_STROKE_COLOR_THEIRS]
 * @property messageStrokeWidthTheirs - stroke width for message sent by other user. Default - [MESSAGE_STROKE_WIDTH_THEIRS]
 * @property textStyleSystemMessage - appearance for system message text
 */
public data class MessageListItemStyle(
    @ColorInt public val messageBackgroundColorMine: Int?,
    @ColorInt public val messageBackgroundColorTheirs: Int?,
    @Deprecated(
        message = "Use MessageListItemStyle::textStyleMine::colorOrNull() instead",
        level = DeprecationLevel.ERROR,
    )
    @ColorInt public val messageTextColorMine: Int?,
    @Deprecated(
        message = "Use MessageListItemStyle::textStyleTheirs::colorOrNull() instead",
        level = DeprecationLevel.ERROR,
    )
    @ColorInt public val messageTextColorTheirs: Int?,
    @ColorInt public val messageLinkTextColorMine: Int?,
    @ColorInt public val messageLinkTextColorTheirs: Int?,
    @ColorInt public val messageLinkBackgroundColorMine: Int,
    @ColorInt public val messageLinkBackgroundColorTheirs: Int,
    public val reactionsEnabled: Boolean,
    public val threadsEnabled: Boolean,
    public val linkDescriptionMaxLines: Int,
    public val textStyleMine: TextStyle,
    public val textStyleTheirs: TextStyle,
    public val textStyleUserName: TextStyle,
    public val textStyleMessageDate: TextStyle,
    public val textStyleThreadCounter: TextStyle,
    public val textStyleLinkTitle: TextStyle,
    public val textStyleLinkDescription: TextStyle,
    @ColorInt public val dateSeparatorBackgroundColor: Int,
    public val textStyleDateSeparator: TextStyle,
    public val reactionsViewStyle: ViewReactionsViewStyle,
    public val editReactionsViewStyle: EditReactionsViewStyle,
    public val iconIndicatorSent: Drawable,
    public val iconIndicatorRead: Drawable,
    public val iconIndicatorPendingSync: Drawable,
    public val textStyleMessageDeleted: TextStyle,
    @ColorInt public val messageDeletedBackground: Int,
    @ColorInt public val messageStrokeColorMine: Int,
    @Px public val messageStrokeWidthMine: Float,
    @ColorInt public val messageStrokeColorTheirs: Int,
    @Px public val messageStrokeWidthTheirs: Float,
    public val textStyleSystemMessage: TextStyle,
) : Serializable {

    @ColorInt
    public fun getStyleTextColor(isMine: Boolean): Int? {
        return if (isMine) textStyleMine.colorOrNull() else textStyleTheirs.colorOrNull()
    }

    @ColorInt
    public fun getStyleLinkTextColor(isMine: Boolean): Int? {
        return if (isMine) messageLinkTextColorMine else messageLinkTextColorTheirs
    }

    internal companion object {
        internal const val VALUE_NOT_SET = Integer.MAX_VALUE

        internal val DEFAULT_LINK_BACKGROUND_COLOR = R.color.stream_ui_blue_alice

        internal val DEFAULT_TEXT_COLOR = R.color.stream_ui_text_color_primary
        internal val DEFAULT_TEXT_SIZE = R.dimen.stream_ui_text_medium
        internal const val DEFAULT_TEXT_STYLE = Typeface.NORMAL

        internal val DEFAULT_TEXT_COLOR_USER_NAME = R.color.stream_ui_text_color_secondary
        internal val DEFAULT_TEXT_SIZE_USER_NAME = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_DATE = R.color.stream_ui_text_color_secondary
        internal val DEFAULT_TEXT_SIZE_DATE = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_THREAD_COUNTER = R.color.stream_ui_accent_blue
        internal val DEFAULT_TEXT_SIZE_THREAD_COUNTER = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_LINK_DESCRIPTION = R.color.stream_ui_text_color_secondary
        internal val DEFAULT_TEXT_SIZE_LINK_DESCRIPTION = R.dimen.stream_ui_text_small

        internal val DEFAULT_TEXT_COLOR_DATE_SEPARATOR = R.color.stream_ui_white
        internal val DEFAULT_TEXT_SIZE_DATE_SEPARATOR = R.dimen.stream_ui_text_small

        internal val MESSAGE_STROKE_COLOR_MINE = R.color.stream_ui_literal_transparent
        internal const val MESSAGE_STROKE_WIDTH_MINE: Float = 0f
        internal val MESSAGE_STROKE_COLOR_THEIRS = R.color.stream_ui_grey_whisper
        internal val MESSAGE_STROKE_WIDTH_THEIRS: Float = 1.dpToPxPrecise()
    }

    internal class Builder(private val attributes: TypedArray, private val context: Context) {
        @ColorInt
        private var messageBackgroundColorMine: Int = VALUE_NOT_SET

        @ColorInt
        private var messageBackgroundColorTheirs: Int = VALUE_NOT_SET

        @ColorInt
        private var messageLinkTextColorMine: Int = VALUE_NOT_SET

        @ColorInt
        private var messageLinkTextColorTheirs: Int = VALUE_NOT_SET

        private var reactionsEnabled: Boolean = true
        private var threadsEnabled: Boolean = true

        private var linkDescriptionMaxLines: Int = 5

        fun messageBackgroundColorMine(
            @StyleableRes messageBackgroundColorMineStyleableId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageBackgroundColorMine = attributes.getColor(messageBackgroundColorMineStyleableId, defaultValue)
        }

        fun messageBackgroundColorTheirs(
            @StyleableRes messageBackgroundColorTheirsId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageBackgroundColorTheirs = attributes.getColor(messageBackgroundColorTheirsId, defaultValue)
        }

        fun messageLinkTextColorMine(
            @StyleableRes messageLinkTextColorMineId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageLinkTextColorMine = attributes.getColor(messageLinkTextColorMineId, defaultValue)
        }

        fun messageLinkTextColorTheirs(
            @StyleableRes messageLinkTextColorTheirsId: Int,
            @ColorInt defaultValue: Int = VALUE_NOT_SET,
        ) = apply {
            messageLinkTextColorTheirs = attributes.getColor(messageLinkTextColorTheirsId, defaultValue)
        }

        fun reactionsEnabled(
            @StyleableRes reactionsEnabled: Int,
            defaultValue: Boolean = true,
        ) = apply {
            this.reactionsEnabled = attributes.getBoolean(reactionsEnabled, defaultValue)
        }

        fun threadsEnabled(
            @StyleableRes threadsEnabled: Int,
            defaultValue: Boolean = true,
        ) = apply {
            this.threadsEnabled = attributes.getBoolean(threadsEnabled, defaultValue)
        }

        fun linkDescriptionMaxLines(
            maxLines: Int,
            defaultValue: Int = 5,
        ) = apply {
            this.linkDescriptionMaxLines = attributes.getInt(maxLines, defaultValue)
        }

        fun build(): MessageListItemStyle {
            val linkBackgroundColorMine =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageLinkBackgroundColorMine,
                    context.getColorCompat(DEFAULT_LINK_BACKGROUND_COLOR)
                )
            val linkBackgroundColorTheirs =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageLinkBackgroundColorTheirs,
                    context.getColorCompat(DEFAULT_LINK_BACKGROUND_COLOR)
                )

            val mediumTypeface = ResourcesCompat.getFont(context, R.font.roboto_medium) ?: Typeface.DEFAULT
            val boldTypeface = ResourcesCompat.getFont(context, R.font.roboto_bold) ?: Typeface.DEFAULT_BOLD

            val textStyleMine = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeMine,
                    context.getDimension(DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorMine,
                    context.getColorCompat(DEFAULT_TEXT_COLOR)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsMine,
                    R.styleable.MessageListView_streamUiMessageTextFontMine,
                    mediumTypeface
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleMine, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleTheirs = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeTheirs,
                    context.getDimension(DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorTheirs,
                    context.getColorCompat(DEFAULT_TEXT_COLOR)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsTheirs,
                    R.styleable.MessageListView_streamUiMessageTextFontTheirs,
                    mediumTypeface
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleTheirs, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleUserName = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeUserName,
                    context.getDimension(DEFAULT_TEXT_SIZE_USER_NAME)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorUserName,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_USER_NAME)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsUserName,
                    R.styleable.MessageListView_streamUiMessageTextFontUserName
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleUserName, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleMessageDate = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeDate,
                    context.getDimension(DEFAULT_TEXT_SIZE_DATE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorDate,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_DATE)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsDate,
                    R.styleable.MessageListView_streamUiMessageTextFontDate
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleDate, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleThreadCounter = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeThreadCounter,
                    context.getDimension(DEFAULT_TEXT_SIZE_THREAD_COUNTER)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorThreadCounter,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_THREAD_COUNTER)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsThreadCounter,
                    R.styleable.MessageListView_streamUiMessageTextFontThreadCounter,
                    mediumTypeface
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleThreadCounter, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleLinkTitle = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeLinkTitle,
                    context.getDimension(DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorLinkTitle,
                    context.getColorCompat(DEFAULT_TEXT_COLOR)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsLinkTitle,
                    R.styleable.MessageListView_streamUiMessageTextFontLinkTitle,
                    boldTypeface
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleLinkTitle, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleLinkDescription = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeLinkDescription,
                    context.getDimension(DEFAULT_TEXT_SIZE_LINK_DESCRIPTION)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorLinkDescription,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_LINK_DESCRIPTION)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsLinkDescription,
                    R.styleable.MessageListView_streamUiMessageTextFontLinkDescription,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleLinkDescription, DEFAULT_TEXT_STYLE)
                .build()

            val dateSeparatorBackgroundColor =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiDateSeparatorBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_overlay_dark)
                )

            val textStyleDateSeparator = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeDateSeparator,
                    context.getDimension(DEFAULT_TEXT_SIZE_DATE_SEPARATOR)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorDateSeparator,
                    context.getColorCompat(DEFAULT_TEXT_COLOR_DATE_SEPARATOR)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsDateSeparator,
                    R.styleable.MessageListView_streamUiMessageTextFontDateSeparator,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleDateSeparator, DEFAULT_TEXT_STYLE)
                .build()

            val reactionsViewStyle = ViewReactionsViewStyle.Companion.Builder(attributes, context)
                .bubbleBorderColor(R.styleable.MessageListView_streamUiMessageReactionsBubbleBorderColorMine)
                .bubbleColorMine(R.styleable.MessageListView_streamUiMessageReactionsBubbleColorMine)
                .bubbleColorTheirs(R.styleable.MessageListView_streamUiMessageReactionsBubbleColorTheirs)
                .build()

            val editReactionsViewStyle = EditReactionsViewStyle.Builder(attributes, context)
                .bubbleColorMine(R.styleable.MessageListView_streamUiEditReactionsBubbleColorMine)
                .bubbleColorTheirs(R.styleable.MessageListView_streamUiEditReactionsBubbleColorTheirs)
                .build()

            val iconIndicatorSent = attributes.getDrawable(
                R.styleable.MessageListView_streamUiIconIndicatorSent
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_check_single)!!
            val iconIndicatorRead = attributes.getDrawable(
                R.styleable.MessageListView_streamUiIconIndicatorRead
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_check_double)!!
            val iconIndicatorPendingSync = attributes.getDrawable(
                R.styleable.MessageListView_streamUiIconIndicatorPendingSync
            ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clock)!!

            val messageDeletedBackground =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiDeletedMessageBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_grey_whisper)
                )

            val textStyleMessageDeleted = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageTextSizeMessageDeleted,
                    context.getDimension(DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageTextColorMessageDeleted,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageTextFontAssetsMessageDeleted,
                    R.styleable.MessageListView_streamUiMessageTextFontMessageDeleted,
                )
                .style(R.styleable.MessageListView_streamUiMessageTextStyleMessageDeleted, Typeface.ITALIC)
                .build()

            val messageStrokeColorMine = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageStrokeColorMine,
                context.getColorCompat(MESSAGE_STROKE_COLOR_MINE)
            )
            val messageStrokeWidthMine =
                attributes.getDimension(
                    R.styleable.MessageListView_streamUiMessageStrokeWidthMine,
                    MESSAGE_STROKE_WIDTH_MINE
                )
            val messageStrokeColorTheirs =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageStrokeColorTheirs,
                    context.getColorCompat(
                        MESSAGE_STROKE_COLOR_THEIRS
                    )
                )
            val messageStrokeWidthTheirs =
                attributes.getDimension(
                    R.styleable.MessageListView_streamUiMessageStrokeWidthTheirs,
                    MESSAGE_STROKE_WIDTH_THEIRS
                )

            val textStyleSystemMessage = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiSystemMessageTextSize,
                    context.getDimension(R.dimen.stream_ui_text_small)
                )
                .color(
                    R.styleable.MessageListView_streamUiSystemMessageTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_secondary)
                )
                .font(
                    R.styleable.MessageListView_streamUiSystemMessageTextFontAssets,
                    R.styleable.MessageListView_streamUiSystemMessageTextFont,
                )
                .style(R.styleable.MessageListView_streamUiSystemMessageTextStyle, Typeface.BOLD)
                .build()

            return MessageListItemStyle(
                messageBackgroundColorMine = messageBackgroundColorMine.nullIfNotSet(),
                messageBackgroundColorTheirs = messageBackgroundColorTheirs.nullIfNotSet(),
                messageTextColorMine = textStyleMine.colorOrNull(),
                messageTextColorTheirs = textStyleTheirs.colorOrNull(),
                messageLinkTextColorMine = messageLinkTextColorMine.nullIfNotSet(),
                messageLinkTextColorTheirs = messageLinkTextColorTheirs.nullIfNotSet(),
                messageLinkBackgroundColorMine = linkBackgroundColorMine,
                messageLinkBackgroundColorTheirs = linkBackgroundColorTheirs,
                reactionsEnabled = reactionsEnabled,
                threadsEnabled = threadsEnabled,
                linkDescriptionMaxLines = linkDescriptionMaxLines,
                textStyleMine = textStyleMine,
                textStyleTheirs = textStyleTheirs,
                textStyleUserName = textStyleUserName,
                textStyleMessageDate = textStyleMessageDate,
                textStyleThreadCounter = textStyleThreadCounter,
                textStyleLinkTitle = textStyleLinkTitle,
                textStyleLinkDescription = textStyleLinkDescription,
                dateSeparatorBackgroundColor = dateSeparatorBackgroundColor,
                textStyleDateSeparator = textStyleDateSeparator,
                reactionsViewStyle = reactionsViewStyle,
                editReactionsViewStyle = editReactionsViewStyle,
                iconIndicatorSent = iconIndicatorSent,
                iconIndicatorRead = iconIndicatorRead,
                iconIndicatorPendingSync = iconIndicatorPendingSync,
                messageDeletedBackground = messageDeletedBackground,
                textStyleMessageDeleted = textStyleMessageDeleted,
                messageStrokeColorMine = messageStrokeColorMine,
                messageStrokeWidthMine = messageStrokeWidthMine,
                messageStrokeColorTheirs = messageStrokeColorTheirs,
                messageStrokeWidthTheirs = messageStrokeWidthTheirs,
                textStyleSystemMessage = textStyleSystemMessage,
            ).let(TransformStyle.messageListItemStyleTransformer::transform)
        }

        private fun Int.nullIfNotSet(): Int? {
            return if (this == VALUE_NOT_SET) null else this
        }
    }
}
