package io.getstream.chat.android.ui.message.input

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.internal.getColorList

public data class MessageInputViewStyle(
    public val attachButtonEnabled: Boolean,
    public val attachButtonIcon: Drawable,
    public val lightningButtonEnabled: Boolean,
    public val lightningButtonIcon: Drawable,
    public val messageInputTextSize: Float,
    public val messageInputTextColor: Int,
    public val messageInputHintTextColor: Int,
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
    @ColorRes public val backgroundColor: Int,
    @ColorRes public val suggestionsBackground: Int,
    public val editTextBackgroundDrawable: Drawable,
    public val customCursorDrawable: Drawable?,
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
                    .font(
                        R.styleable.MessageInputView_streamUiMessageInputFontAssets,
                        R.styleable.MessageInputView_streamUiMessageInputFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiMessageInputTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val commandsEnabled = a.getBoolean(R.styleable.MessageInputView_streamUiCommandsEnabled, true)

                val commandsBackground = a.getColor(
                    R.styleable.MessageInputView_streamUiSuggestionBackgroundColor,
                    ContextCompat.getColor(context, R.color.stream_ui_white)
                )

                val commandsTitleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCommandsTitleTextSize,
                        R.dimen.stream_ui_text_medium
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsTitleTextColor,
                        ContextCompat.getColor(context, R.color.stream_ui_text_color_secondary)
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
                        R.dimen.stream_ui_text_medium
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsNameTextColor,
                        ContextCompat.getColor(context, R.color.stream_ui_black)
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
                        R.dimen.stream_ui_text_medium
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionTextColor,
                        ContextCompat.getColor(context, R.color.stream_ui_black)
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
                        R.dimen.stream_ui_text_medium
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMentionsUserNameTextColor,
                        ContextCompat.getColor(context, R.color.stream_ui_black)
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
                        R.dimen.stream_ui_text_medium
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMentionsNameTextColor,
                        ContextCompat.getColor(context, R.color.stream_ui_text_color_secondary)
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

                var backgroundColor: Int
                context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.background)).use {
                    backgroundColor = it.getColor(0, ContextCompat.getColor(context, R.color.stream_ui_white))
                }

                val customCursorDrawable = a.getDrawable(
                    R.styleable.MessageInputView_streamUiMessageInputCustomCursorDrawable
                )

                val editTextBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageInputView_streamUiMessageInputEditTextBackgroundDrawable
                ) ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_shape_edit_text_round)!!

                return MessageInputViewStyle(
                    attachButtonEnabled = attachButtonEnabled,
                    attachButtonIcon = attachButtonIcon,
                    lightningButtonEnabled = lightningButtonEnabled,
                    lightningButtonIcon = lightningButtonIcon,
                    messageInputTextSize = messageInputTextSize,
                    messageInputTextColor = messageInputTextColor,
                    messageInputHintTextColor = messageInputHintTextColor,
                    messageInputTextStyle = messageInputTextStyle,
                    messageInputScrollbarEnabled = messageInputScrollbarEnabled,
                    messageInputScrollbarFadingEnabled = messageInputScrollbarFadingEnabled,
                    sendButtonEnabled = sendButtonEnabled,
                    sendButtonEnabledIcon = sendButtonEnabledIcon,
                    sendButtonDisabledIcon = sendButtonDisabledIcon,
                    showSendAlsoToChannelCheckbox = showSendAlsoToChannelCheckbox,
                    commandsEnabled = commandsEnabled,
                    commandsTitleTextStyle = commandsTitleTextStyle,
                    suggestionsBackground = commandsBackground,
                    commandsNameTextStyle = commandsNameTextStyle,
                    commandsDescriptionTextStyle = commandsDescriptionTextStyle,
                    mentionsEnabled = mentionsEnabled,
                    backgroundColor = backgroundColor,
                    mentionsUsernameTextStyle = mentionsUsernameTextStyle,
                    mentionsNameTextStyle = mentionsNameTextStyle,
                    editTextBackgroundDrawable = editTextBackgroundDrawable,
                    customCursorDrawable = customCursorDrawable,
                ).let(TransformStyle.messageInputStyleTransformer::transform)
            }
        }
    }
}
