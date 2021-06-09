package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 * Style for view holders used inside [MessageListView].
 * Use this class together with [TransformStyle.messageListItemStyleTransformer] to change styles programmatically.
 *
 */
public data class MessageReplyStyle(
    @ColorInt
    public val messageBackgroundColorMine: Int?,
    @ColorInt
    public val messageBackgroundColorTheirs: Int?,
    @ColorInt
    public val linkBackgroundColorMine: Int?,
    @ColorInt
    public val linkBackgroundColorTheirs: Int?,
    public val textStyleMine: TextStyle,
    public val textStyleTheirs: TextStyle,
    public val linkStyleMine: TextStyle,
    public val linkStyleTheirs: TextStyle,
) {
    internal companion object {
        operator fun invoke(attributes: TypedArray, context: Context): MessageReplyStyle {
            val messageBackgroundColorMine: Int = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyBackgroundColorMine,
                VALUE_NOT_SET
            )
            val messageBackgroundColorTheirs: Int = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyBackgroundColorTheirs,
                VALUE_NOT_SET
            )
            val linkBackgroundColorMine = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyLinkBackgroundColorMine,
                VALUE_NOT_SET
            )
            val linkBackgroundColorTheirs = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyLinkBackgroundColorTheirs,
                VALUE_NOT_SET
            )
            val mediumTypeface = ResourcesCompat.getFont(context, R.font.roboto_medium) ?: Typeface.DEFAULT
            val textStyleMine = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageReplyTextSizeMine,
                    context.getDimension(MessageListItemStyle.DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyTextColorMine,
                    context.getColorCompat(MessageListItemStyle.DEFAULT_TEXT_COLOR)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageReplyTextFontAssetsMine,
                    R.styleable.MessageListView_streamUiMessageReplyTextFontMine,
                    mediumTypeface
                )
                .style(
                    R.styleable.MessageListView_streamUiMessageReplyTextStyleMine,
                    MessageListItemStyle.DEFAULT_TEXT_STYLE
                )
                .build()

            val textStyleTheirs = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageReplyTextSizeTheirs,
                    context.getDimension(DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyTextColorTheirs,
                    context.getColorCompat(DEFAULT_TEXT_COLOR)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageReplyTextFontAssetsTheirs,
                    R.styleable.MessageListView_streamUiMessageReplyTextFontTheirs,
                    mediumTypeface
                )
                .style(
                    R.styleable.MessageListView_streamUiMessageReplyTextStyleTheirs,
                    DEFAULT_TEXT_STYLE
                )
                .build()

            val textStyleLinkTheirs = TextStyle.Builder(attributes)
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyLinkColorTheirs,
                    VALUE_NOT_SET
                )
                .build()

            val textStyleLinkMine = TextStyle.Builder(attributes)
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyLinkColorMine,
                    VALUE_NOT_SET
                )
                .build()

            return MessageReplyStyle(
                messageBackgroundColorMine = messageBackgroundColorMine,
                messageBackgroundColorTheirs = messageBackgroundColorTheirs,
                linkStyleMine = textStyleLinkMine,
                linkStyleTheirs = textStyleLinkTheirs,
                linkBackgroundColorMine = linkBackgroundColorMine,
                linkBackgroundColorTheirs = linkBackgroundColorTheirs,
                textStyleMine = textStyleMine,
                textStyleTheirs = textStyleTheirs,
            ).let(TransformStyle.messageReplyStyleTransformer::transform)
        }

        private const val VALUE_NOT_SET = Integer.MAX_VALUE
        private val DEFAULT_TEXT_COLOR = R.color.stream_ui_text_color_primary
        private val DEFAULT_TEXT_SIZE = R.dimen.stream_ui_text_medium
        private const val DEFAULT_TEXT_STYLE = Typeface.NORMAL
    }
}
