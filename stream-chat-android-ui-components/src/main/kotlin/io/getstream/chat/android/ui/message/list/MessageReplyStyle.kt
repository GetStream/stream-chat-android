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
    public val textStyleMine: TextStyle,
    public val textStyleTheirs: TextStyle,
) {
    internal companion object {
        operator fun invoke(attributes: TypedArray, context: Context): MessageReplyStyle {
            val messageBackgroundColorMine: Int = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyBackgroundColorMine,
                context.getColorCompat(R.color.stream_ui_white),
            )
            val messageBackgroundColorTheirs: Int = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyBackgroundColorTheirs,
                context.getColorCompat(R.color.stream_ui_white),
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
                    context.getDimension(MessageListItemStyle.DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyTextColorTheirs,
                    context.getColorCompat(MessageListItemStyle.DEFAULT_TEXT_COLOR)
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageReplyTextFontAssetsTheirs,
                    R.styleable.MessageListView_streamUiMessageReplyTextFontTheirs,
                    mediumTypeface
                )
                .style(
                    R.styleable.MessageListView_streamUiMessageReplyTextStyleTheirs,
                    MessageListItemStyle.DEFAULT_TEXT_STYLE
                )
                .build()
            return MessageReplyStyle(
                messageBackgroundColorMine = messageBackgroundColorMine,
                messageBackgroundColorTheirs = messageBackgroundColorTheirs,
                textStyleMine = textStyleMine,
                textStyleTheirs = textStyleTheirs,
            ).let(TransformStyle.messageReplyStyleTransformer::transform)
        }

        internal const val VALUE_NOT_SET = Integer.MAX_VALUE
    }
}
