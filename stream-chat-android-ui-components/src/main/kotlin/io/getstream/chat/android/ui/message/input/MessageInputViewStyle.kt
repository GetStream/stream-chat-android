package io.getstream.chat.android.ui.message.input

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.DrawableCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.internal.getColorList
import io.getstream.chat.android.ui.common.style.TextStyle

private const val DEFAULT_ATTACHMENT_MAX_SIZE_MB = 20

public data class MessageInputViewStyle(
    public val attachButtonEnabled: Boolean,
    public val attachButtonIcon: Drawable,
    public val lightningButtonEnabled: Boolean,
    public val lightningButtonIcon: Drawable,
    @Deprecated("Use messageInputTextStyle") public val messageInputTextSize: Float,
    @Deprecated("Use messageInputTextStyle") public val messageInputTextColor: Int,
    @Deprecated("Use messageInputTextStyle") public val messageInputHintTextColor: Int,
    public val messageInputTextStyle: TextStyle,
    public val messageInputScrollbarEnabled: Boolean,
    public val messageInputScrollbarFadingEnabled: Boolean,
    public val sendButtonEnabled: Boolean,
    public val sendButtonEnabledIcon: Drawable,
    public val sendButtonDisabledIcon: Drawable,
    public val showSendAlsoToChannelCheckbox: Boolean,
    public val commandsEnabled: Boolean,
    public val commandsTitleTextStyle: TextStyle,
    public val commandsNameTextStyle: TextStyle,
    public val commandsDescriptionTextStyle: TextStyle,
    public val mentionsEnabled: Boolean,
    public val mentionsUsernameTextStyle: TextStyle,
    public val mentionsNameTextStyle: TextStyle,
    public val mentionsIcon: Drawable,
    @ColorRes public val backgroundColor: Int,
    @ColorRes public val suggestionsBackground: Int,
    public val editTextBackgroundDrawable: Drawable,
    public val customCursorDrawable: Drawable?,
    public val attachmentMaxFileSize: Int,
    public val dividerBackground: Drawable,
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

                val messageInputTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiMessageInputTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_size_input)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMessageInputTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiMessageInputFontAssets,
                        R.styleable.MessageInputView_streamUiMessageInputFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiMessageInputTextStyle,
                        Typeface.NORMAL
                    )
                    .hint(
                        R.styleable.MessageInputView_streamUiMessageInputHintText,
                        context.getString(R.string.stream_ui_message_input_default_hint)
                    )
                    .hintColor(
                        R.styleable.MessageInputView_streamUiMessageInputHintTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_hint)
                    )
                    .build()

                val commandsEnabled = a.getBoolean(R.styleable.MessageInputView_streamUiCommandsEnabled, true)

                val commandsBackground = a.getColor(
                    R.styleable.MessageInputView_streamUiSuggestionBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white)
                )

                val commandsTitleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCommandsTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiCommandsTitleFontAssets,
                        R.styleable.MessageInputView_streamUiCommandsTitleFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiCommandsTitleStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val commandsNameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCommandsNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiCommandsNameFontAssets,
                        R.styleable.MessageInputView_streamUiCommandsNameFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiCommandsNameStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val commandsDescriptionTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionFontAssets,
                        R.styleable.MessageInputView_streamUiCommandsDescriptionFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val mentionsUsernameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiMentionsUserNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMentionsUserNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiMentionsUserNameFontAssets,
                        R.styleable.MessageInputView_streamUiMentionsUserNameFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiMentionsUserNameStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val mentionsNameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiMentionsNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMentionsNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiMentionsNameFontAssets,
                        R.styleable.MessageInputView_streamUiMentionsNameFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiMentionsNameStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val mentionsIcon: Drawable =
                    a.getDrawable(
                        R.styleable.MessageInputView_streamUiMentionsIcon
                    ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mention)!!

                var backgroundColor: Int
                context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.background)).use {
                    backgroundColor = it.getColor(0, context.getColorCompat(R.color.stream_ui_white))
                }

                val customCursorDrawable = a.getDrawable(
                    R.styleable.MessageInputView_streamUiMessageInputCustomCursorDrawable
                )

                val editTextBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageInputView_streamUiMessageInputEditTextBackgroundDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_shape_edit_text_round)!!

                val dividerBackground = a.getDrawable(
                    R.styleable.MessageInputView_streamUiMessageInputDividerBackgroundDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_divider)!!

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
                    messageInputTextStyle = messageInputTextStyle,
                    messageInputScrollbarEnabled = messageInputScrollbarEnabled,
                    messageInputScrollbarFadingEnabled = messageInputScrollbarFadingEnabled,
                    messageInputHintTextColor = messageInputHintTextColor,
                    sendButtonEnabled = sendButtonEnabled,
                    sendButtonEnabledIcon = sendButtonEnabledIcon,
                    sendButtonDisabledIcon = sendButtonDisabledIcon,
                    showSendAlsoToChannelCheckbox = showSendAlsoToChannelCheckbox,
                    commandsEnabled = commandsEnabled,
                    commandsTitleTextStyle = commandsTitleTextStyle,
                    commandsNameTextStyle = commandsNameTextStyle,
                    commandsDescriptionTextStyle = commandsDescriptionTextStyle,
                    mentionsEnabled = mentionsEnabled,
                    mentionsUsernameTextStyle = mentionsUsernameTextStyle,
                    mentionsNameTextStyle = mentionsNameTextStyle,
                    mentionsIcon = mentionsIcon,
                    backgroundColor = backgroundColor,
                    suggestionsBackground = commandsBackground,
                    editTextBackgroundDrawable = editTextBackgroundDrawable,
                    customCursorDrawable = customCursorDrawable,
                    dividerBackground = dividerBackground,
                    attachmentMaxFileSize = attachmentMaxFileSize,
                ).let(TransformStyle.messageInputStyleTransformer::transform)
            }
        }
    }
}
