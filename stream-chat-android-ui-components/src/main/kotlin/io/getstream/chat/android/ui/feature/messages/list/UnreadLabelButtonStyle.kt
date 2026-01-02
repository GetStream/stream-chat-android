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
import androidx.annotation.StyleableRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension

/**
 * Style for [UnreadLabelButtonStyle].
 *
 * @property unreadLabelButtonEnabled Enables/disables view which allows to scroll to the latest messages.
 * Default value is true.
 * @property unreadLabelButtonColor [UnreadLabelButtonStyle] background color.
 * Default value is [R.color.stream_ui_overlay_dark].
 * @property unreadLabelButtonRippleColor [UnreadLabelButtonStyle] ripple color.
 * Default value is [R.color.stream_ui_white_smoke].
 * @property unreadLabelButtonTextStyle Text appearance of the unread count label.
 */
public data class UnreadLabelButtonStyle(
    public val unreadLabelButtonEnabled: Boolean,
    @ColorInt public val unreadLabelButtonColor: Int,
    @ColorInt public val unreadLabelButtonRippleColor: Int,
    public val unreadLabelButtonTextStyle: TextStyle,
) : ViewStyle {

    internal class Builder(private val context: Context, private val attrs: TypedArray) {
        private var unreadLabelButtonEnabled: Boolean = true

        @ColorInt private var unreadLabelButtonColor: Int = 0

        @ColorInt private var unreadLabelButtonRippleColor: Int = 0

        fun unreadLabelButtonEnabled(
            @StyleableRes unreadLabelButtonEnabledStyleableId: Int,
            defaultValue: Boolean,
        ) = apply {
            unreadLabelButtonEnabled = attrs.getBoolean(unreadLabelButtonEnabledStyleableId, defaultValue)
        }

        fun unreadLabelButtonColor(
            @StyleableRes unreadLabelButtonColorStyleableId: Int,
            @ColorInt defaultValue: Int,
        ) = apply {
            unreadLabelButtonColor = attrs.getColor(unreadLabelButtonColorStyleableId, defaultValue)
        }

        fun unreadLabelButtonRippleColor(
            @StyleableRes unreadLabelButtonRippleColorStyleableId: Int,
            @ColorInt defaultColor: Int,
        ) = apply {
            unreadLabelButtonRippleColor = attrs.getColor(unreadLabelButtonRippleColorStyleableId, defaultColor)
        }

        fun build(): UnreadLabelButtonStyle {
            val unreadLabelButtonTextStyle = TextStyle.Builder(attrs)
                .size(
                    R.styleable.MessageListView_streamUiUnreadLabelButtonTextSize,
                    context.getDimension(R.dimen.stream_ui_scroll_button_unread_badge_text_size),
                )
                .color(
                    R.styleable.MessageListView_streamUiUnreadLabelButtonTextColor,
                    context.getColorCompat(R.color.stream_ui_literal_white),
                )
                .font(
                    R.styleable.MessageListView_streamUiUnreadLabelButtonFontAssets,
                    R.styleable.MessageListView_streamUiUnreadLabelButtonTextFont,
                )
                .style(R.styleable.MessageListView_streamUiUnreadLabelButtonTextStyle, Typeface.BOLD)
                .build()

            return UnreadLabelButtonStyle(
                unreadLabelButtonEnabled = unreadLabelButtonEnabled,
                unreadLabelButtonColor = unreadLabelButtonColor,
                unreadLabelButtonRippleColor = unreadLabelButtonRippleColor,
                unreadLabelButtonTextStyle = unreadLabelButtonTextStyle,
            ).let(TransformStyle.unreadLabelButtonStyleTransformer::transform)
        }
    }
}
