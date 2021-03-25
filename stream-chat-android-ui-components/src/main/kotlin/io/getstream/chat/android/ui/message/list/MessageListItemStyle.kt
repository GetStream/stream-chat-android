package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle
import java.io.Serializable

public data class MessageListItemStyle(
    @ColorInt public val messageBackgroundColorMine: Int?,
    @ColorInt public val messageBackgroundColorTheirs: Int?,
    @Deprecated("Use MessageListItemStyle::textStyleMine::colorOrNull() instead")
    @ColorInt public val messageTextColorMine: Int?,
    @Deprecated("Use MessageListItemStyle::textStyleTheirs::colorOrNull() instead")
    @ColorInt public val messageTextColorTheirs: Int?,
    @ColorInt public val messageLinkTextColorMine: Int?,
    @ColorInt public val messageLinkTextColorTheirs: Int?,
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
    public val textStyleDateSeparator: TextStyle,
    public val reactionsViewStyle: ViewReactionsViewStyle,
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
            val mediumTypeface = ResourcesCompat.getFont(context, R.font.roboto_medium) ?: Typeface.DEFAULT
            val boldTypeface = ResourcesCompat.getFont(context, R.font.roboto_bold) ?: Typeface.DEFAULT_BOLD

            val textStyleMine = TextStyle.Builder(attributes)
                .size(R.styleable.MessageListView_streamUiMessageTextSizeMine, context.getDimension(DEFAULT_TEXT_SIZE))
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

            return MessageListItemStyle(
                messageBackgroundColorMine = messageBackgroundColorMine.nullIfNotSet(),
                messageBackgroundColorTheirs = messageBackgroundColorTheirs.nullIfNotSet(),
                messageTextColorMine = textStyleMine.colorOrNull(),
                messageTextColorTheirs = textStyleTheirs.colorOrNull(),
                messageLinkTextColorMine = messageLinkTextColorMine.nullIfNotSet(),
                messageLinkTextColorTheirs = messageLinkTextColorTheirs.nullIfNotSet(),
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
                textStyleDateSeparator = textStyleDateSeparator,
                reactionsViewStyle = reactionsViewStyle,
            ).let(TransformStyle.messageListItemStyleTransformer::transform)
        }

        private fun Int.nullIfNotSet(): Int? {
            return if (this == VALUE_NOT_SET) null else this
        }
    }
}
