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
* @property mentionsEnabled Enables/disables mentions.
* @property backgroundColor background color of MessageInputView
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
    public val mentionsEnabled: Boolean,
    @ColorInt public val backgroundColor: Int,
    public val editTextBackgroundDrawable: Drawable,
    public val customCursorDrawable: Drawable?,
    public val attachmentMaxFileSize: Int,
    public val dividerBackground: Drawable,
    public val attachmentSelectionDialogStyle: AttachmentSelectionDialogStyle,
    public val commandInputCancelIcon: Drawable,
    public val commandInputBadgeIcon: Drawable,
    public val commandInputBadgeBackgroundDrawable: Drawable,
    public val commandInputBadgeTextStyle: TextStyle,
    public val fileNameTextStyle: TextStyle,
    public val fileSizeTextStyle: TextStyle,
    public val fileCheckboxSelectorDrawable: Drawable,
    @ColorInt public val fileCheckboxTextColor: Int,
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

                val allowAccessToCameraIcon =
                    a.getDrawable(R.styleable.MessageInputView_streamUiAllowAccessToCameraIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!

                val allowAccessToFilesIcon =
                    a.getDrawable(R.styleable.MessageInputView_streamUiAllowAccessToFilesIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!

                val allowAccessToGalleryIcon =
                    a.getDrawable(R.styleable.MessageInputView_streamUiAllowAccessToGalleryIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!

                val allowAccessToGalleryText = a.getText(R.styleable.MessageInputView_streamUiAllowAccessToGalleryText)
                    ?: context.getString(R.string.stream_ui_message_input_gallery_access)

                val allowAccessToFilesText = a.getText(R.styleable.MessageInputView_streamUiAllowAccessToFilesText)
                    ?: context.getString(R.string.stream_ui_message_input_files_access)

                val allowAccessToCameraText = a.getText(R.styleable.MessageInputView_streamUiAllowAccessToCameraText)
                    ?: context.getString(R.string.stream_ui_message_input_camera_access)

                val grantPermissionsTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiGrantPermissionsTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiGrantPermissionsTextColor,
                        context.getColorCompat(R.color.stream_ui_accent_blue)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiGrantPermissionsFontAssets,
                        R.styleable.MessageInputView_streamUiGrantPermissionsFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiGrantPermissionsTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val recentFilesTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesTextColor,
                        context.getColorCompat(R.color.stream_ui_black)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesFontAssets,
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val recentFilesText = a.getText(R.styleable.MessageInputView_streamUiAttachmentsRecentFilesText)
                    ?: context.getString(R.string.stream_ui_message_input_recent_files)

                val fileManagerIcon = a.getDrawable(R.styleable.MessageInputView_streamUiAttachmentsFileManagerIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

                val attachmentDialogStyle = AttachmentSelectionDialogStyle(
                    pictureAttachmentIcon = pictureAttachmentIcon,
                    pictureAttachmentIconTint = pictureAttachmentIconTint,
                    fileAttachmentIcon = fileAttachmentIcon,
                    fileAttachmentIconTint = fileAttachmentIconTint,
                    cameraAttachmentIcon = cameraAttachmentIcon,
                    cameraAttachmentIconTint = cameraAttachmentIconTint,
                    allowAccessToCameraIcon = allowAccessToCameraIcon,
                    allowAccessToFilesIcon = allowAccessToFilesIcon,
                    allowAccessToGalleryIcon = allowAccessToGalleryIcon,
                    allowAccessToGalleryText = allowAccessToGalleryText.toString(),
                    allowAccessToFilesText = allowAccessToFilesText.toString(),
                    allowAccessToCameraText = allowAccessToCameraText.toString(),
                    grantPermissionsTextStyle = grantPermissionsTextStyle,
                    recentFilesTextStyle = recentFilesTextStyle,
                    recentFilesText = recentFilesText.toString(),
                    fileManagerIcon = fileManagerIcon,
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

                val fileNameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiAttachmentsFileNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiAttachmentsFileNameTextColor,
                        context.getColorCompat(R.color.stream_ui_black)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiAttachmentsFileNameFontAssets,
                        R.styleable.MessageInputView_streamUiAttachmentsFileNameFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiAttachmentsFileNameTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val fileSizeTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiAttachmentsFileSizeTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiAttachmentsFileSizeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiAttachmentsFileSizeFontAssets,
                        R.styleable.MessageInputView_streamUiAttachmentsFileSizeFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiAttachmentsFileSizeTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val fileCheckboxTextColor =
                    a.getColor(
                        R.styleable.MessageInputView_streamUiFileCheckBoxSelectorTextColor,
                        context.getColorCompat(R.color.stream_ui_white_snow)
                    )

                val fileCheckboxSelectorDrawable =
                    a.getDrawable(R.styleable.MessageInputView_streamUiFileCheckBoxSelectorDrawable)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

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
                    mentionsEnabled = mentionsEnabled,
                    backgroundColor = backgroundColor,
                    editTextBackgroundDrawable = editTextBackgroundDrawable,
                    customCursorDrawable = customCursorDrawable,
                    dividerBackground = dividerBackground,
                    attachmentMaxFileSize = attachmentMaxFileSize,
                    attachmentSelectionDialogStyle = attachmentDialogStyle,
                    commandInputCancelIcon = commandInputCancelIcon,
                    commandInputBadgeIcon = commandInputBadgeIcon,
                    commandInputBadgeBackgroundDrawable = commandInputBadgeBackgroundDrawable,
                    commandInputBadgeTextStyle = commandInputBadgeTextStyle,
                    fileNameTextStyle = fileNameTextStyle,
                    fileSizeTextStyle = fileSizeTextStyle,
                    fileCheckboxTextColor = fileCheckboxTextColor,
                    fileCheckboxSelectorDrawable = fileCheckboxSelectorDrawable,
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
