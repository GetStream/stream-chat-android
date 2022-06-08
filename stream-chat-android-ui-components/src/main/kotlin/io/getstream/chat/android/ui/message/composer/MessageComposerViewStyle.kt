/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 * Style for [MessageComposerView].
 *
 * @param sendButtonEnabled If the button to send message is enabled.
 * @param sendButtonIcon The icon for the button to send message.
 * @param cooldownTimerTextStyle The text style that will be used for cooldown timer.
 * @param cooldownTimerBackgroundDrawable Background drawable for cooldown timer.
 */
public data class MessageComposerViewStyle(

    // Command suggestions content

    // Mention suggestions content

    // Center content

    // Leading content

    // Footer content

    // Header content

    // Trailing content
    public val sendButtonEnabled: Boolean,
    public val sendButtonIcon: Drawable,
    public val cooldownTimerTextStyle: TextStyle,
    public val cooldownTimerBackgroundDrawable: Drawable,
) {
    public companion object {
        internal operator fun invoke(context: Context, attrs: AttributeSet?): MessageComposerViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageComposerView,
                R.attr.streamUiMessageComposerViewStyle,
                R.style.StreamUi_MessageComposerView,
            ).use { a ->
                /**
                 * Command suggestions content
                 */

                /**
                 * Mention suggestions content
                 */

                /**
                 * Center content
                 */

                /**
                 * Leading content
                 */

                /**
                 * Footer content
                 */

                /**
                 * Header content
                 */

                /**
                 * Trailing content
                 */

                val sendButtonEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerSendButtonEnabled,
                    true
                )

                val sendButtonIcon =
                    a.getDrawable(R.styleable.MessageComposerView_streamUiMessageComposerSendButtonIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_send_message)!!

                val cooldownTimerTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextColor,
                        context.getColorCompat(R.color.stream_ui_literal_white)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val cooldownTimerBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerBackgroundDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_cooldown_badge_background)!!

                return MessageComposerViewStyle(
                    // Command suggestions content

                    // Mention suggestions content

                    // Center content

                    // Leading content

                    // Footer content

                    // Header content

                    // Trailing content
                    sendButtonEnabled = sendButtonEnabled,
                    sendButtonIcon = sendButtonIcon,
                    cooldownTimerTextStyle = cooldownTimerTextStyle,
                    cooldownTimerBackgroundDrawable = cooldownTimerBackgroundDrawable,
                ).let(TransformStyle.messageComposerStyleTransformer::transform)
            }
        }
    }
}
