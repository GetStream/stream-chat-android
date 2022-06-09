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
 * @param messageInputScrollbarEnabled If the vertical scrollbar should be drawn or not.
 * @param messageInputScrollbarFadingEnabled If the vertical edges should be faded on scroll or not.
 *
 * @param attachmentsButtonVisible If the button to pick attachments is displayed.
 * @param attachmentsButtonIconDrawable The icon for the attachments button.
 * @param attachmentsButtonRippleColor Ripple color of the attachments button.
 * @param commandsButtonVisible If the button to select commands is displayed.
 * @param commandsButtonIconDrawable The icon for the commands button.
 * @param commandsButtonRippleColor Ripple color of the commands button.
 * @param alsoSendToChannelCheckboxVisible If the checkbox to send a message to the channel is displayed.
 * @param alsoSendToChannelCheckboxDrawable The drawable that will be used for the checkbox.
 * @param alsoSendToChannelCheckboxText The text that will be displayed next to the checkbox.
 * @param alsoSendToChannelCheckboxTextStyle The text style that will be used for the checkbox text.
 * @param editModeIconDrawable The icon displayed in top left corner when the user edits a message.
 * @param replyModeIconDrawable The icon displayed in top left corner when the user replies to a message.
 * @param dismissModeIconDrawable The icon for the button that dismisses edit or reply mode.
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
    public val messageInputScrollbarEnabled: Boolean,
    public val messageInputScrollbarFadingEnabled: Boolean,
    // Leading content
    public val attachmentsButtonVisible: Boolean,
    public val attachmentsButtonIconDrawable: Drawable,
    @ColorInt public val attachmentsButtonRippleColor: Int?,
    public val commandsButtonVisible: Boolean,
    public val commandsButtonIconDrawable: Drawable,
    @ColorInt public val commandsButtonRippleColor: Int?,
    // Footer content
    public val alsoSendToChannelCheckboxVisible: Boolean,
    public val alsoSendToChannelCheckboxDrawable: Drawable?,
    public val alsoSendToChannelCheckboxText: CharSequence?,
    public val alsoSendToChannelCheckboxTextStyle: TextStyle,
    // Header content
    public val editModeIconDrawable: Drawable,
    public val replyModeIconDrawable: Drawable,
    public val dismissModeIconDrawable: Drawable,
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

                val messageInputScrollbarEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiMessageInputScrollbarEnabled,
                    false
                )
                val messageInputScrollbarFadingEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiMessageInputScrollbarFadingEnabled,
                    false
                )

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

                val alsoSendToChannelCheckboxVisible = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxVisible,
                    true
                )

                val alsoSendToChannelCheckboxDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxDrawable
                )

                val alsoSendToChannelCheckboxText: CharSequence? = a.getText(
                    R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxText
                )

                val alsoSendToChannelCheckboxTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextFont
                    )
                    .style(
                        R.styleable.MessageComposerView_streamUiMessageComposerAlsoSendToChannelCheckboxTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                /**
                 * Header content
                 */

                val editModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerEditModeIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_edit)!!

                val replyModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerReplyModeIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_arrow_curve_left)!!

                val dismissModeIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerEditModeIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                /**
                 * Trailing content
                 */

                val sendMessageButtonEnabled = a.getBoolean(
                    R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonEnabled,
                    true
                )

                val sendMessageButtonIconDrawable = a.getDrawable(
                    R.styleable.MessageComposerView_streamUiMessageComposerSendMessageButtonIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_send_message)!!

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
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextFontAssets,
                        R.styleable.MessageComposerView_streamUiMessageComposerCooldownTimerTextFont
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
                    messageInputScrollbarEnabled = messageInputScrollbarEnabled,
                    messageInputScrollbarFadingEnabled = messageInputScrollbarFadingEnabled,
                    // Leading content
                    attachmentsButtonVisible = attachmentsButtonVisible,
                    attachmentsButtonIconDrawable = attachmentsButtonIconDrawable,
                    attachmentsButtonRippleColor = attachmentsButtonRippleColor,
                    commandsButtonVisible = commandsButtonVisible,
                    commandsButtonIconDrawable = commandsButtonIconDrawable,
                    commandsButtonRippleColor = commandsButtonRippleColor,
                    // Footer content
                    alsoSendToChannelCheckboxVisible = alsoSendToChannelCheckboxVisible,
                    alsoSendToChannelCheckboxDrawable = alsoSendToChannelCheckboxDrawable,
                    alsoSendToChannelCheckboxText = alsoSendToChannelCheckboxText,
                    alsoSendToChannelCheckboxTextStyle = alsoSendToChannelCheckboxTextStyle,
                    // Header content
                    editModeIconDrawable = editModeIconDrawable,
                    replyModeIconDrawable = replyModeIconDrawable,
                    dismissModeIconDrawable = dismissModeIconDrawable,
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
