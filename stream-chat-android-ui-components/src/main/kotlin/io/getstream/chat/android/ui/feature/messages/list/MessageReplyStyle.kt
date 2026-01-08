/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.feature.messages.list

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.MessageReplyStyle.Companion.MESSAGE_STROKE_COLOR_MINE
import io.getstream.chat.android.ui.feature.messages.list.MessageReplyStyle.Companion.MESSAGE_STROKE_COLOR_THEIRS
import io.getstream.chat.android.ui.feature.messages.list.MessageReplyStyle.Companion.MESSAGE_STROKE_WIDTH_MINE
import io.getstream.chat.android.ui.feature.messages.list.MessageReplyStyle.Companion.MESSAGE_STROKE_WIDTH_THEIRS
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension

/**
 * Style for view holders used inside [MessageListView] allowing to customize message "reply" view.
 * Use this class together with [TransformStyle.messageReplyStyleTransformer] to change styles programmatically.
 *
 * @param showUserAvatar Whether to show user avatar in the reply view. Default value is `true`.
 * @property messageBackgroundColorMine Background color for message sent by the current user. Default value is [R.color.stream_ui_grey_gainsboro].
 * @property messageBackgroundColorTheirs Background color for message sent by other user. Default value is [R.color.stream_ui_white].
 * @property linkBackgroundColorMine Background color of links in the message sent by the current user.
 * @property linkBackgroundColorTheirs Background color of links in the message sent by the other user.
 * @property linkStyleMine Appearance for message link sent by the current user.
 * @property linkStyleTheirs Appearance for message link sent by other users.
 * @property textStyleMine Appearance for message text sent by the current user.
 * @property textStyleTheirs Appearance for message text sent by other users.
 * @property messageStrokeColorMine Stroke color for message sent by the current user. Default value is [MESSAGE_STROKE_COLOR_MINE].
 * @property messageStrokeWidthMine Stroke width for message sent by the current user. Default value is [MESSAGE_STROKE_WIDTH_MINE].
 * @property messageStrokeColorTheirs Stroke color for message sent by other user. Default value is [MESSAGE_STROKE_COLOR_THEIRS].
 * @property messageStrokeWidthTheirs Stroke width for message sent by other user. Default value is [MESSAGE_STROKE_WIDTH_THEIRS].
 */
public data class MessageReplyStyle(
    public val showUserAvatar: Boolean = true,
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
) : ViewStyle {
    internal companion object {
        operator fun invoke(attributes: TypedArray, context: Context): MessageReplyStyle {
            val showUserAvatar = attributes.getBoolean(
                R.styleable.MessageListView_streamUiMessageReplyShowUserAvatar,
                true,
            )
            val messageBackgroundColorMine: Int = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyBackgroundColorMine,
                VALUE_NOT_SET,
            )
            val messageBackgroundColorTheirs: Int = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyBackgroundColorTheirs,
                VALUE_NOT_SET,
            )
            val linkBackgroundColorMine = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyLinkBackgroundColorMine,
                VALUE_NOT_SET,
            )
            val linkBackgroundColorTheirs = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyLinkBackgroundColorTheirs,
                VALUE_NOT_SET,
            )
            val mediumTypeface = ResourcesCompat.getFont(context, R.font.stream_roboto_medium) ?: Typeface.DEFAULT
            val textStyleMine = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageReplyTextSizeMine,
                    context.getDimension(DEFAULT_TEXT_SIZE),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyTextColorMine,
                    context.getColorCompat(DEFAULT_TEXT_COLOR),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageReplyTextFontAssetsMine,
                    R.styleable.MessageListView_streamUiMessageReplyTextFontMine,
                    mediumTypeface,
                )
                .style(R.styleable.MessageListView_streamUiMessageReplyTextStyleMine, DEFAULT_TEXT_STYLE)
                .build()

            val textStyleTheirs = TextStyle.Builder(attributes)
                .size(
                    R.styleable.MessageListView_streamUiMessageReplyTextSizeTheirs,
                    context.getDimension(DEFAULT_TEXT_SIZE),
                )
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyTextColorTheirs,
                    context.getColorCompat(DEFAULT_TEXT_COLOR),
                )
                .font(
                    R.styleable.MessageListView_streamUiMessageReplyTextFontAssetsTheirs,
                    R.styleable.MessageListView_streamUiMessageReplyTextFontTheirs,
                    mediumTypeface,
                )
                .style(
                    R.styleable.MessageListView_streamUiMessageReplyTextStyleTheirs,
                    DEFAULT_TEXT_STYLE,
                )
                .build()

            val textStyleLinkTheirs = TextStyle.Builder(attributes)
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyLinkColorTheirs,
                    VALUE_NOT_SET,
                )
                .build()

            val textStyleLinkMine = TextStyle.Builder(attributes)
                .color(
                    R.styleable.MessageListView_streamUiMessageReplyLinkColorMine,
                    VALUE_NOT_SET,
                )
                .build()

            val messageStrokeColorMine = attributes.getColor(
                R.styleable.MessageListView_streamUiMessageReplyStrokeColorMine,
                context.getColorCompat(MESSAGE_STROKE_COLOR_MINE),
            )
            val messageStrokeWidthMine =
                attributes.getDimension(
                    R.styleable.MessageListView_streamUiMessageReplyStrokeWidthMine,
                    MESSAGE_STROKE_WIDTH_MINE,
                )
            val messageStrokeColorTheirs =
                attributes.getColor(
                    R.styleable.MessageListView_streamUiMessageReplyStrokeColorTheirs,
                    context.getColorCompat(MESSAGE_STROKE_COLOR_THEIRS),
                )
            val messageStrokeWidthTheirs =
                attributes.getDimension(
                    R.styleable.MessageListView_streamUiMessageReplyStrokeWidthTheirs,
                    MESSAGE_STROKE_WIDTH_THEIRS,
                )

            return MessageReplyStyle(
                showUserAvatar = showUserAvatar,
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

        private val MESSAGE_STROKE_WIDTH_THEIRS: Float by lazy { 1.dpToPxPrecise() }
        private const val VALUE_NOT_SET = Integer.MAX_VALUE
        internal val DEFAULT_TEXT_COLOR = R.color.stream_ui_text_color_primary
        internal val DEFAULT_TEXT_SIZE = R.dimen.stream_ui_text_medium
        internal const val DEFAULT_TEXT_STYLE = Typeface.NORMAL
        internal val MESSAGE_STROKE_COLOR_MINE = R.color.stream_ui_literal_transparent
        internal const val MESSAGE_STROKE_WIDTH_MINE: Float = 0f
        internal val MESSAGE_STROKE_COLOR_THEIRS = R.color.stream_ui_grey_whisper
    }
}
