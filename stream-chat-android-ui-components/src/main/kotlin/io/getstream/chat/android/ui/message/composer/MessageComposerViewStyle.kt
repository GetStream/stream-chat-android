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
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 * Style for [MessageComposerView].
 *
 * @param dividerBackgroundDrawable The background of the divider at the top.
 *
 * @param attachmentsButtonVisible If the button to pick attachments is displayed.
 * @param attachmentsButtonIconDrawable The icon for the attachments button.
 * @param attachmentsButtonRippleColor Ripple color of the attachments button.
 * @param commandsButtonVisible If the button to select commands is displayed.
 * @param commandsButtonIconDrawable The icon for the commands button.
 * @param commandsButtonRippleColor Ripple color of the commands button.
 *
 * @param sendMessageButtonEnabled If the button to send message is enabled.
 * @param sendMessageButtonIconDrawable The icon for the button to send message.
 * @param cooldownTimerTextStyle The text style that will be used for cooldown timer.
 * @param cooldownTimerBackgroundDrawable Background drawable for cooldown timer.
 */
public data class MessageComposerViewStyle(
    // Root content
    public val dividerBackgroundDrawable: Drawable,
    // Command suggestions content

    // Mention suggestions content

    // Center content

    // Leading content
    public val attachmentsButtonVisible: Boolean,
    public val attachmentsButtonIconDrawable: Drawable,
    @ColorInt public val attachmentsButtonRippleColor: Int?,
    public val commandsButtonVisible: Boolean,
    public val commandsButtonIconDrawable: Drawable,
    @ColorInt public val commandsButtonRippleColor: Int?,
    // Footer content

    // Header content

    // Trailing content
    public val sendMessageButtonEnabled: Boolean,
    public val sendMessageButtonIconDrawable: Drawable,
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
                 * Root content
                 */
                val dividerBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerDividerBackgroundDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_divider)!!
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

                val attachmentsButtonVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonVisible,
                    true
                )
                val attachmentsButtonIconDrawable =
                    a.getDrawable(R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonIconDrawable)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_attach)!!

                val attachmentsButtonRippleColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerAttachmentsButtonRippleColor
                )

                val commandsButtonVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonVisible,
                    true
                )

                val commandsButtonIconDrawable =
                    a.getDrawable(R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonIconDrawable)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command)!!

                val commandsButtonRippleColor = a.getColorOrNull(
                    R.styleable.MessageComposerView_streamUiMessageComposerCommandsButtonRippleColor
                )

                /**
                 * Footer content
                 */

                /**
                 * Header content
                 */

                /**
                 * Trailing content
                 */

                val sendMessageButtonEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonEnabled,
                    true
                )

                val sendMessageButtonIconDrawable =
                    a.getDrawable(R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonIconDrawable)
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
                    // Root content
                    dividerBackgroundDrawable = dividerBackgroundDrawable,
                    // Command suggestions content

                    // Mention suggestions content

                    // Center content

                    // Leading content
                    attachmentsButtonVisible = attachmentsButtonVisible,
                    attachmentsButtonIconDrawable = attachmentsButtonIconDrawable,
                    attachmentsButtonRippleColor = attachmentsButtonRippleColor,
                    commandsButtonVisible = commandsButtonVisible,
                    commandsButtonIconDrawable = commandsButtonIconDrawable,
                    commandsButtonRippleColor = commandsButtonRippleColor,
                    // Footer content

                    // Header content

                    // Trailing content
                    sendMessageButtonEnabled = sendMessageButtonEnabled,
                    sendMessageButtonIconDrawable = sendMessageButtonIconDrawable,
                    cooldownTimerTextStyle = cooldownTimerTextStyle,
                    cooldownTimerBackgroundDrawable = cooldownTimerBackgroundDrawable,
                ).let(TransformStyle.messageComposerStyleTransformer::transform)
            }
        }
    }
}
