package io.getstream.chat.android.ui.message.input

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.core.graphics.drawable.DrawableCompat
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.internal.getColorList
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogStyle

private const val DEFAULT_ATTACHMENT_MAX_SIZE_MB = 20

/*
* Style for MessageInputView
*
* Use this class to style MessageInputView programmatically. You can pass this class to TransformStyle.messageInputStyleTransformer
* to change the configuration of the View.
*
* @constructor Create the data class with all the information necessary to customize MessageInputView
*
* @property attachButtonEnabled Enables/disabled the attachment button. If this parameter is set as false, the button gets invisible.
* @property attachButtonIcon Selects the icon for the attachment button.
* @property commandsButtonEnabled Enables/disables the commands button. If this parameter is set as false, the button gets invisible.
* @property commandsButtonIcon Selects the icon for the commands button.
* @property messageInputTextSize Selects the size of the text in the input edit text. Deprecated - Use messageInputTextStyle
* @property messageInputTextColor Selects the colour of the text in the input edit text. Deprecated - Use messageInputTextStyle
* @property messageInputHintTextColor Selects the colour of the hint text in the input edit text. Deprecated - Use messageInputTextStyle
* @property messageInputTextStyle Select the style of the EditText of the input. Use to customise size, colour, hint, hint colour, font and style (ex: bold)
* @property messageInputScrollbarEnabled Enables/disables scroll bar
* @property messageInputScrollbarFadingEnabled Enables/disables enables/disables FadingEdge for the EditText of the input
* @property sendButtonEnabled Enables/disables the button to send messages.
* @property sendButtonEnabledIcon Sets the icon for the button to send messages.
* @property sendButtonDisabledIcon Sets the icon for the button send messages in the disabled state
* @property showSendAlsoToChannelCheckbox Show the checkbox to send a message to the channel when inside a thread.
* @property commandsEnabled Enables/disables commands for the MessageInputView.
* @property commandsTitleTextStyle Sets the styles for title for the commands box. Use to customise size, colour, font and style (ex: bold)
* @property commandsNameTextStyle Sets the styles for the name of the command in the command list. Use to customise size, colour, font and style (ex: bold)
* @property commandsDescriptionTextStyle Sets the styles for the description of the command in the command list. Use to customise size, colour, font and style (ex: bold)
* @property mentionsEnabled Enables/disables mentions.
* @property mentionsUsernameTextStyle Configure the appearance for username in the mention list
* @property mentionsNameTextStyle Configure the appearance for username in the mention list
* @property mentionsIcon Icon for mentions. It is normally "@"
* @property backgroundColor background color of MessageInputView
* @property suggestionsBackground background color of the suggestions box (command and mentions)
* @property editTextBackgroundDrawable background color of message input box inside MessageInputView
* @property customCursorDrawable custom cursor of message input box inside MessageInputView
* @property attachmentMaxFileSize the max attachment size. Be aware that currently the back end of Stream allow 20MB as
* the max size, use this only if you use your own backend.
* @property dividerBackground the background of the divider in the top of MessageInputView.
* @property attachmentDialogStyle style for attachment selection dialog
* @property commandInputCancelIcon - icon for cancel button. Default - [R.drawable.stream_ui_ic_clear]
* @property commandInputBadgeIcon - icon inside command badge. Default - [R.drawable.stream_ui_ic_command_white]
* @property commandInputBadgeBackgroundDrawable - background shape of command badge. Default - [R.drawable.stream_ui_shape_command_background]
* @property commandInputBadgeTextStyle - text appearance of command name inside command badge
*/
public data class MessageInputViewStyle(
    public val attachButtonEnabled: Boolean,
    public val attachButtonIcon: Drawable,
    public val commandsButtonEnabled: Boolean,
    public val commandsButtonIcon: Drawable,
    @Deprecated("Use messageInputTextStyle") public val messageInputTextSize: Float,
    @Deprecated("Use messageInputTextStyle") @ColorInt public val messageInputTextColor: Int,
    @Deprecated("Use messageInputTextStyle") @ColorInt public val messageInputHintTextColor: Int,
    public val messageInputTextStyle: TextStyle,
    public val messageInputScrollbarEnabled: Boolean,
    public val messageInputScrollbarFadingEnabled: Boolean,
    public val sendButtonEnabled: Boolean,
    public val sendButtonEnabledIcon: Drawable,
    public val sendButtonDisabledIcon: Drawable,
    public val showSendAlsoToChannelCheckbox: Boolean,
    public val sendAlsoToChannelCheckboxDrawable: Drawable?,
    public val sendAlsoToChannelCheckboxGroupChatText: CharSequence?,
    public val sendAlsoToChannelCheckboxDirectChatText: CharSequence?,
    public val sendAlsoToChannelCheckboxTextStyle: TextStyle,
    public val commandsEnabled: Boolean,
    public val commandsTitleTextStyle: TextStyle,
    public val commandsNameTextStyle: TextStyle,
    public val commandsDescriptionTextStyle: TextStyle,
    public val mentionsEnabled: Boolean,
    public val mentionsUsernameTextStyle: TextStyle,
    public val mentionsNameTextStyle: TextStyle,
    public val mentionsIcon: Drawable,
    @ColorInt public val backgroundColor: Int,
    @ColorInt public val suggestionsBackground: Int,
    public val editTextBackgroundDrawable: Drawable,
    public val customCursorDrawable: Drawable?,
    public val attachmentMaxFileSize: Int,
    public val dividerBackground: Drawable,
    public val attachmentSelectionDialogStyle: AttachmentSelectionDialogStyle,
    public val commandInputCancelIcon: Drawable,
    public val commandInputBadgeIcon: Drawable,
    public val commandInputBadgeBackgroundDrawable: Drawable,
    public val commandInputBadgeTextStyle: TextStyle,
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
                            setTintListIfNeeded(
                                typedArray = a,
                                drawable = this,
                                normalColorIndex = R.styleable.MessageInputView_streamUiAttachButtonIconColor,
                                selectedColorIndex = R.styleable.MessageInputView_streamUiAttachButtonIconPressedColor,
                                disabledColorIndex = R.styleable.MessageInputView_streamUiAttachButtonIconDisabledColor,
                            )
                        }

                val lightningButtonEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiLightningButtonEnabled,
                    true
                )
                val lightningButtonIcon = a.getDrawable(R.styleable.MessageInputView_streamUiLightningButtonIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command)!!
                        .apply {
                            setTintListIfNeeded(
                                typedArray = a,
                                drawable = this,
                                normalColorIndex = R.styleable.MessageInputView_streamUiLightningButtonIconColor,
                                selectedColorIndex = R.styleable.MessageInputView_streamUiLightningButtonIconPressedColor,
                                disabledColorIndex = R.styleable.MessageInputView_streamUiLightningButtonIconDisabledColor,
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
                val sendButtonEnabledIcon = a.getDrawable(R.styleable.MessageInputView_streamUiSendButtonEnabledIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_filled_up_arrow)!!
                        .apply {
                            setTintListIfNeeded(
                                typedArray = a,
                                drawable = this,
                                normalColorIndex = R.styleable.MessageInputView_streamUiSendButtonEnabledIconColor,
                                selectedColorIndex = R.styleable.MessageInputView_streamUiSendButtonPressedIconColor,
                                disabledColorIndex = R.styleable.MessageInputView_streamUiSendButtonDisabledIconColor,
                            )
                        }
                val sendButtonDisabledIcon = a.getDrawable(R.styleable.MessageInputView_streamUiSendButtonDisabledIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_filled_right_arrow)!!
                        .apply {
                            setTintListIfNeeded(
                                typedArray = a,
                                drawable = this,
                                normalColorIndex = R.styleable.MessageInputView_streamUiSendButtonDisabledIconColor,
                                selectedColorIndex = R.styleable.MessageInputView_streamUiSendButtonDisabledIconColor,
                                disabledColorIndex = R.styleable.MessageInputView_streamUiSendButtonDisabledIconColor,
                            )
                        }

                val showSendAlsoToChannelCheckbox = a.getBoolean(
                    R.styleable.MessageInputView_streamUiShowSendAlsoToChannelCheckbox,
                    true
                )
                val sendAlsoToChannelCheckboxGroupChatText: CharSequence? = a.getText(
                    R.styleable.MessageInputView_streamUiSendAlsoToChannelCheckboxGroupChatText
                )
                val sendAlsoToChannelCheckboxDirectChatText: CharSequence? = a.getText(
                    R.styleable.MessageInputView_streamUiSendAlsoToChannelCheckboxDirectChatText
                )
                val sendAlsoToChannelCheckboxTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiSendAlsoToChannelCheckboxTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiSendAlsoToChannelCheckboxTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiSendAlsoToChannelCheckboxTextFontAssets,
                        R.styleable.MessageInputView_streamUiSendAlsoToChannelCheckboxTextFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiSendAlsoToChannelCheckboxTextStyle,
                        Typeface.NORMAL
                    )
                    .build()
                val sendAlsoToChannelCheckboxDrawable =
                    a.getDrawable(R.styleable.MessageInputView_streamUiSendAlsoToChannelCheckboxDrawable)

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
                        context.getString(R.string.stream_ui_message_input_hint)
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

                // Override this to set cursor drawable programmatically. Only available for API 29+
                val customCursorDrawable = null

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

                val pictureAttachmentIcon = a.getDrawable(
                    R.styleable.MessageInputView_streamUiPictureAttachmentIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!

                val pictureAttachmentIconTint =
                    a.getColorStateList(R.styleable.MessageInputView_streamUiPictureAttachmentIconTint)

                val fileAttachmentIcon = a.getDrawable(
                    R.styleable.MessageInputView_streamUiFileAttachmentIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!

                val fileAttachmentIconTint =
                    a.getColorStateList(R.styleable.MessageInputView_streamUiFileAttachmentIconTint)

                val cameraAttachmentIcon = a.getDrawable(
                    R.styleable.MessageInputView_streamUiCameraAttachmentIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!

                val cameraAttachmentIconTint =
                    a.getColorStateList(R.styleable.MessageInputView_streamUiCameraAttachmentIconTint)

                val attachmentDialogStyle = AttachmentSelectionDialogStyle(
                    pictureAttachmentIcon = pictureAttachmentIcon,
                    pictureAttachmentIconTint = pictureAttachmentIconTint,
                    fileAttachmentIcon = fileAttachmentIcon,
                    fileAttachmentIconTint = fileAttachmentIconTint,
                    cameraAttachmentIcon = cameraAttachmentIcon,
                    cameraAttachmentIconTint = cameraAttachmentIconTint,
                )

                val commandInputCancelIcon = a.getDrawable(
                    R.styleable.MessageInputView_streamUiCommandInputCancelIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                val commandInputBadgeBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageInputView_streamUiCommandInputBadgeBackgroundDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_shape_command_background)!!

                val commandInputBadgeIcon = a.getDrawable(
                    R.styleable.MessageInputView_streamUiCommandInputBadgeIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command_white)!!

                val commandInputBadgeTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCommandInputBadgeTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandInputBadgeTextColor,
                        context.getColorCompat(R.color.stream_ui_literal_white)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiCommandInputBadgeFontAssets,
                        R.styleable.MessageInputView_streamUiCommandInputBadgeFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiCommandInputBadgeStyle,
                        Typeface.BOLD
                    )
                    .build()

                return MessageInputViewStyle(
                    attachButtonEnabled = attachButtonEnabled,
                    attachButtonIcon = attachButtonIcon,
                    commandsButtonEnabled = lightningButtonEnabled,
                    commandsButtonIcon = lightningButtonIcon,
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
                    sendAlsoToChannelCheckboxGroupChatText = sendAlsoToChannelCheckboxGroupChatText,
                    sendAlsoToChannelCheckboxDirectChatText = sendAlsoToChannelCheckboxDirectChatText,
                    sendAlsoToChannelCheckboxDrawable = sendAlsoToChannelCheckboxDrawable,
                    sendAlsoToChannelCheckboxTextStyle = sendAlsoToChannelCheckboxTextStyle,
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
                    attachmentSelectionDialogStyle = attachmentDialogStyle,
                    commandInputCancelIcon = commandInputCancelIcon,
                    commandInputBadgeIcon = commandInputBadgeIcon,
                    commandInputBadgeBackgroundDrawable = commandInputBadgeBackgroundDrawable,
                    commandInputBadgeTextStyle = commandInputBadgeTextStyle,
                ).let(TransformStyle.messageInputStyleTransformer::transform)
            }
        }

        private fun setTintListIfNeeded(
            typedArray: TypedArray,
            drawable: Drawable,
            @StyleableRes normalColorIndex: Int,
            @StyleableRes selectedColorIndex: Int,
            @StyleableRes disabledColorIndex: Int,
        ) {
            val normalColor = typedArray.getColorOrNull(normalColorIndex)
            val selectedColor = typedArray.getColorOrNull(selectedColorIndex)
            val disabledColor = typedArray.getColorOrNull(disabledColorIndex)

            if (normalColor != null && selectedColor != null && disabledColor != null) {
                DrawableCompat.setTintList(
                    drawable,
                    getColorList(
                        normalColor = normalColor,
                        selectedColor = selectedColor,
                        disabledColor = disabledColor,
                    ),
                )
            }
        }
    }
}
