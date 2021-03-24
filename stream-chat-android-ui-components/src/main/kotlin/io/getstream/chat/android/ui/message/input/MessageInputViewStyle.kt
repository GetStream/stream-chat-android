package io.getstream.chat.android.ui.message.input

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.graphics.drawable.DrawableCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.internal.getColorList

private const val DEFAULT_ATTACHMENT_MAX_SIZE_MB = 20

public data class MessageInputViewStyle(
    public val attachButtonEnabled: Boolean,
    public val attachButtonIcon: Drawable,
    public val lightningButtonEnabled: Boolean,
    public val lightningButtonIcon: Drawable,
    public val messageInputTextSize: Float,
    public val messageInputTextColor: Int,
    public val messageInputHintTextColor: Int,
    public val messageInputScrollbarEnabled: Boolean,
    public val messageInputScrollbarFadingEnabled: Boolean,
    public val sendButtonEnabled: Boolean,
    public val sendButtonEnabledIcon: Drawable,
    public val sendButtonDisabledIcon: Drawable,
    public val showSendAlsoToChannelCheckbox: Boolean,
    public val mentionsEnabled: Boolean,
    public val commandsEnabled: Boolean,
    public val attachmentMaxFileSize: Int
) {

    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): MessageInputViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageInputView,
                0,
                0,
            ).use { a ->
                val attachButtonEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiAttachButtonEnabled,
                    true
                )
                val attachButtonIcon = a.getDrawable(R.styleable.MessageInputView_streamUiAttachButtonIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_attach)!!
                        .apply {
                            DrawableCompat.setTintList(
                                this,
                                getColorList(
                                    normalColor = a.getColor(
                                        R.styleable.MessageInputView_streamUiAttachButtonIconColor,
                                        context.getColorCompat(R.color.stream_ui_grey)
                                    ),
                                    selectedColor = a.getColor(
                                        R.styleable.MessageInputView_streamUiAttachButtonIconPressedColor,
                                        context.getColorCompat(R.color.stream_ui_accent_blue)
                                    ),
                                    disabledColor = a.getColor(
                                        R.styleable.MessageInputView_streamUiAttachButtonIconDisabledColor,
                                        context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                                    )
                                )
                            )
                        }

                val lightningButtonEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiLightningButtonEnabled,
                    true
                )
                val lightningButtonIcon = a.getDrawable(R.styleable.MessageInputView_streamUiLightningButtonIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command)!!
                        .apply {
                            DrawableCompat.setTintList(
                                this,
                                getColorList(
                                    normalColor = a.getColor(
                                        R.styleable.MessageInputView_streamUiLightningButtonIconColor,
                                        context.getColorCompat(R.color.stream_ui_grey)
                                    ),
                                    selectedColor = a.getColor(
                                        R.styleable.MessageInputView_streamUiLightningButtonIconPressedColor,
                                        context.getColorCompat(R.color.stream_ui_accent_blue)
                                    ),
                                    disabledColor = a.getColor(
                                        R.styleable.MessageInputView_streamUiLightningButtonIconDisabledColor,
                                        context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                                    )
                                )
                            )
                        }

                val messageInputTextSize = a.getDimensionPixelSize(
                    R.styleable.MessageInputView_streamUiMessageInputTextSize,
                    context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_size_input)
                ).toFloat()
                val messageInputTextColor = a.getColor(
                    R.styleable.MessageInputView_streamUiMessageInputTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )
                val messageInputHintTextColor = a.getColor(
                    R.styleable.MessageInputView_streamUiMessageInputHintTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_hint)
                )
                val messageInputScrollbarEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiMessageInputScrollbarEnabled,
                    false
                )
                val messageInputScrollbarFadingEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiMessageInputScrollbarFadingEnabled,
                    false
                )

                val sendButtonEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiSendButtonEnabled,
                    true
                )
                val sendButtonDisabledIconColor = a.getColor(
                    R.styleable.MessageInputView_streamUiSendButtonDisabledIconColor,
                    context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                )
                val sendButtonEnabledIcon = a.getDrawable(R.styleable.MessageInputView_streamUiSendButtonEnabledIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_filled_up_arrow)!!
                        .apply {
                            DrawableCompat.setTintList(
                                this,
                                getColorList(
                                    normalColor = a.getColor(
                                        R.styleable.MessageInputView_streamUiSendButtonEnabledIconColor,
                                        context.getColorCompat(R.color.stream_ui_accent_blue)
                                    ),
                                    selectedColor = a.getColor(
                                        R.styleable.MessageInputView_streamUiSendButtonPressedIconColor,
                                        context.getColorCompat(R.color.stream_ui_accent_blue)
                                    ),
                                    disabledColor = sendButtonDisabledIconColor
                                )
                            )
                        }
                val sendButtonDisabledIcon = a.getDrawable(R.styleable.MessageInputView_streamUiSendButtonDisabledIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_filled_right_arrow)!!
                        .apply {
                            DrawableCompat.setTintList(
                                this,
                                getColorList(
                                    normalColor = sendButtonDisabledIconColor,
                                    selectedColor = sendButtonDisabledIconColor,
                                    disabledColor = sendButtonDisabledIconColor
                                )
                            )
                        }

                val showSendAlsoToChannelCheckbox = a.getBoolean(
                    R.styleable.MessageInputView_streamUiShowSendAlsoToChannelCheckbox,
                    true
                )
                val mentionsEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiMentionsEnabled,
                    true
                )
                val commandsEnabled = a.getBoolean(R.styleable.MessageInputView_streamUiCommandsEnabled, true)

                val attachmentMaxFileSize = a.getInt(
                    R.styleable.MessageInputView_streamUiAttachmentMaxFileSizeMb,
                    DEFAULT_ATTACHMENT_MAX_SIZE_MB
                )

                return MessageInputViewStyle(
                    attachButtonEnabled = attachButtonEnabled,
                    attachButtonIcon = attachButtonIcon,
                    lightningButtonEnabled = lightningButtonEnabled,
                    lightningButtonIcon = lightningButtonIcon,
                    messageInputTextSize = messageInputTextSize,
                    messageInputTextColor = messageInputTextColor,
                    messageInputHintTextColor = messageInputHintTextColor,
                    messageInputScrollbarEnabled = messageInputScrollbarEnabled,
                    messageInputScrollbarFadingEnabled = messageInputScrollbarFadingEnabled,
                    sendButtonEnabled = sendButtonEnabled,
                    sendButtonEnabledIcon = sendButtonEnabledIcon,
                    sendButtonDisabledIcon = sendButtonDisabledIcon,
                    showSendAlsoToChannelCheckbox = showSendAlsoToChannelCheckbox,
                    mentionsEnabled = mentionsEnabled,
                    commandsEnabled = commandsEnabled,
                    attachmentMaxFileSize = attachmentMaxFileSize,
                ).let(TransformStyle.messageInputStyleTransformer::transform)
            }
        }
    }
}
