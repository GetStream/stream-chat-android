package io.getstream.chat.android.ui.message.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.list.MessageReplyStyle.Companion.MESSAGE_STROKE_COLOR_MINE
import io.getstream.chat.android.ui.message.list.MessageReplyStyle.Companion.MESSAGE_STROKE_COLOR_THEIRS
import io.getstream.chat.android.ui.message.list.MessageReplyStyle.Companion.MESSAGE_STROKE_WIDTH_MINE
import io.getstream.chat.android.ui.message.list.MessageReplyStyle.Companion.MESSAGE_STROKE_WIDTH_THEIRS

/**
 * Style for view holders used inside [MessageListView] allowing to customize message "reply" view.
 * Use this class together with [TransformStyle.messageReplyStyleTransformer] to change styles programmatically.
 *
 * @property messageBackgroundColorMine - background color for message sent by the current user. Default - [R.color.stream_ui_grey_gainsboro]
 * @property messageBackgroundColorTheirs - background color for message sent by other user. Default - [R.color.stream_ui_white]
 * @property messageLinkTextColorMine - color for links sent by the current user. Default - [R.color.stream_ui_accent_blue]
 * @property messageLinkTextColorTheirs - color for links sent by other user. Default - [R.color.stream_ui_accent_blue]
 * @property messageLinkBackgroundColorMine - background color for message with link, sent by the current user. Default - [R.color.stream_ui_blue_alice]
 * @property messageLinkBackgroundColorTheirs - background color for message with link, sent by other user. Default - [R.color.stream_ui_blue_alice]
 * @property linkStyleMine - appearance for message link sent by the current user
 * @property linkStyleTheirs - appearance for message link sent by other user
 * @property textStyleMine - appearance for message text sent by the current user
 * @property textStyleTheirs - appearance for message text sent by other user
 * @property messageStrokeColorMine - stroke color for message sent by the current user. Default - [MESSAGE_STROKE_COLOR_MINE]
 * @property messageStrokeWidthMine - stroke width for message sent by the current user. Default - [MESSAGE_STROKE_WIDTH_MINE]
 * @property messageStrokeColorTheirs - stroke color for message sent by other user. Default - [MESSAGE_STROKE_COLOR_THEIRS]
 * @property messageStrokeWidthTheirs - stroke width for message sent by other user. Default - [MESSAGE_STROKE_WIDTH_THEIRS]
 */
public data class MessageReplyStyle(
    @ColorInt public val messageBackgroundColorMine: Int,
    @ColorInt public val messageBackgroundColorTheirs: Int,
    @ColorInt public val linkBackgroundColorMine: Int,
    @ColorInt public val linkBackgroundColorTheirs: Int,
    public val textStyleMine: TextStyle,
    public val textStyleTheirs: TextStyle,
    public val linkStyleMine: TextStyle,
    public val linkStyleTheirs: TextStyle,
    @ColorInt public val messageStrokeColorMine: Int,
    @Px public val messageStrokeWidthMine: Float,
    @ColorInt public val messageStrokeColorTheirs: Int,
    @Px public val messageStrokeWidthTheirs: Float,
) {
    internal companion object {
        operator fun invoke(attributes: TypedArray, context: Context, forceLightMode: Boolean): MessageReplyStyle {
            val messageBackgroundColorMine: Int = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyBackgroundColorMine,
                VALUE_NOT_SET
            )
            val messageBackgroundColorTheirs: Int = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyBackgroundColorTheirs,
                context.getColorCompat(
                    R.color.stream_ui_white,
                    R.color.stream_ui_literal_white,
                    forceLightMode
                )
            )
            val linkBackgroundColorMine = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyLinkBackgroundColorMine,
                context.getColorCompat(
                    R.color.stream_ui_literal_blue_alice,
                    R.color.stream_ui_literal_blue_alice,
                    forceLightMode
                )
            )
            val linkBackgroundColorTheirs = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyLinkBackgroundColorTheirs,
                context.getColorCompat(
                    R.color.stream_ui_literal_blue_alice,
                    R.color.stream_ui_literal_blue_alice,
                    forceLightMode
                )
            )
            val mediumTypeface = ResourcesCompat.getFont(context, R.font.roboto_medium) ?: Typeface.DEFAULT
            val textStyleMine = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageReplyTextSizeMine,
                    context.getDimension(DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyTextColorMine,
                    context.getColorCompat(
                        DEFAULT_TEXT_COLOR,
                        R.color.stream_ui_literal_black,
                        forceLightMode
                    )
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageReplyTextFontAssetsMine,
                    R.styleable.MessageListView_streamUiMessageReplyTextFontMine,
                    mediumTypeface
                )
                .style(R.styleable.MessageListView_streamUiMessageReplyTextStyleMine, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleTheirs = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageReplyTextSizeTheirs,
                    context.getDimension(DEFAULT_TEXT_SIZE)
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyTextColorTheirs,
                    context.getColorCompat(
                        DEFAULT_TEXT_COLOR,
                        R.color.stream_ui_literal_black,
                        forceLightMode
                    )
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

            val messageStrokeColorMine = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyStrokeColorMine,
                context.getColorCompat(MESSAGE_STROKE_COLOR_MINE)
            )
            val messageStrokeWidthMine =
                attributes.getDimension(
                    R.styleable.MessageListView_streamUiMessageReplyStrokeWidthMine,
                    MESSAGE_STROKE_WIDTH_MINE
                )
            val messageStrokeColorTheirs =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageReplyStrokeColorTheirs,
                    context.getColorCompat(
                        MESSAGE_STROKE_COLOR_THEIRS,
                        R.color.stream_ui_literal_grey_whisper,
                        forceLightMode
                    )
                )
            val messageStrokeWidthTheirs =
                attributes.getDimension(
                    R.styleable.MessageListView_streamUiMessageReplyStrokeWidthTheirs,
                    MESSAGE_STROKE_WIDTH_THEIRS
                )

            return MessageReplyStyle(
                messageBackgroundColorMine = messageBackgroundColorMine,
                messageBackgroundColorTheirs = messageBackgroundColorTheirs,
                linkStyleMine = textStyleLinkMine,
                linkStyleTheirs = textStyleLinkTheirs,
                linkBackgroundColorMine = linkBackgroundColorMine,
                linkBackgroundColorTheirs = linkBackgroundColorTheirs,
                textStyleMine = textStyleMine,
                textStyleTheirs = textStyleTheirs,
                messageStrokeColorMine = messageStrokeColorMine,
                messageStrokeColorTheirs = messageStrokeColorTheirs,
                messageStrokeWidthMine = messageStrokeWidthMine,
                messageStrokeWidthTheirs = messageStrokeWidthTheirs,
            ).let(TransformStyle.messageReplyStyleTransformer::transform)
        }

        private val MESSAGE_STROKE_WIDTH_THEIRS: Float = 1.dpToPxPrecise()
        private const val VALUE_NOT_SET = Integer.MAX_VALUE
        private val DEFAULT_TEXT_COLOR = R.color.stream_ui_text_color_primary
        private val DEFAULT_TEXT_SIZE = R.dimen.stream_ui_text_medium
        private const val DEFAULT_TEXT_STYLE = Typeface.NORMAL
        internal val MESSAGE_STROKE_COLOR_MINE = R.color.stream_ui_literal_transparent
        internal const val MESSAGE_STROKE_WIDTH_MINE: Float = 0f
        internal val MESSAGE_STROKE_COLOR_THEIRS = R.color.stream_ui_grey_whisper
    }
}
