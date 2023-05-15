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

package io.getstream.chat.android.ui.message.input

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import com.getstream.sdk.chat.utils.AttachmentConstants
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getColorStateListCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogStyle
import io.getstream.chat.android.ui.message.list.MessageReplyStyle

/**
 * Style for [MessageInputView].
 *
 * Use this class to style MessageInputView programmatically. You can pass this class to TransformStyle.messageInputStyleTransformer
 * to change the configuration of the View.
 *
 * @constructor Create the data class with all the information necessary to customize MessageInputView.
 *
 * @property attachButtonEnabled Enables/disabled the attachment button. If this parameter is set as false, the button gets invisible.
 * @property attachButtonIcon Selects the icon for the attachment button.
 * @property commandsButtonEnabled Enables/disables the commands button. If this parameter is set as false, the button gets invisible.
 * @property commandsButtonIcon Selects the icon for the commands button.
 * @property messageInputTextStyle Select the style of the EditText of the input. Use to customise size, colour, hint, hint colour, font and style (ex: bold).
 * @property messageInputScrollbarEnabled Enables/disables scroll bar.
 * @property messageInputScrollbarFadingEnabled Enables/disables enables/disables FadingEdge for the EditText of the input.
 * @property sendButtonEnabled Enables/disables the button to send messages.
 * @property sendButtonEnabledIcon Sets the icon for the button to send messages.
 * @property sendButtonDisabledIcon Sets the icon for the button send messages in the disabled state.
 * @property showSendAlsoToChannelCheckbox Show the checkbox to send a message to the channel when inside a thread.
 * @property commandsEnabled Enables/disables commands for the MessageInputView.
 * @property mentionsEnabled Enables/disables mentions.
 * @property backgroundColor Background color of MessageInputView.
 * @property editTextBackgroundDrawable Background color of message input box inside MessageInputView.
 * @property customCursorDrawable Custom cursor of message input box inside MessageInputView.
 * @property attachmentMaxFileSize The max attachment size. Limited to 100 MB by Stream's CDN. Use your own CDN if you
 * need to upload larger files.
 * @property dividerBackground The background of the divider in the top of MessageInputView.
 * @property attachmentSelectionDialogStyle Style for attachment selection dialog.
 * @property commandInputCancelIcon Icon for cancel button. Default value is [R.drawable.stream_ui_ic_clear].
 * @property commandInputBadgeIcon Icon inside command badge. Default value is [R.drawable.stream_ui_ic_command_white].
 * @property commandInputBadgeBackgroundDrawable Background shape of command badge. Default value is [R.drawable.stream_ui_shape_command_background].
 * @property commandInputBadgeTextStyle Text appearance of command name inside command badge.
 * @property cooldownTimerTextStyle Text appearance for cooldown timer text displayed over the send button
 * @property cooldownTimerBackgroundDrawable Background drawable for cooldown timer. Default value is [R.drawable.stream_ui_cooldown_badge_background].
 * @property fileCheckboxSelectedDrawable Background for selector of files list in selected state. Default value is [R.drawable.stream_ui_circle_blue].
 * @property fileCheckboxDeselectedDrawable Background for selector of files list in deselected state. Default value is [R.drawable.stream_ui_ic_file_manager].
 * @property maxAttachmentsCount Maximum number of attachments for single message. Cannot by greater than 10. Default value is 10.
 * @property editInputModeIcon Icon displayed in MessageInputView's top left corner when user edits the message. Default value is [R.drawable.stream_ui_ic_edit]
 * @property replyInputModeIcon Icon displayed in MessageInputView's top left corner when user replies to the message. Default value is [R.drawable.stream_ui_ic_arrow_curve_left]
 * @property commandButtonRippleColor Ripple color of the command button. Default value is [colorControlHighlight]
 * @property attachmentButtonRippleColor Ripple color of the attachment button. Default value is [colorControlHighlight]
 * @property messageInputInputType The [InputType] to be applied to the message input edit text.
 * @param messageReplyBackgroundColor Sets the background color of the quoted message bubble visible in the input
 * when replying to a message. Applied both when replying to messages owned by the currently logged-in user and messages
 * owned by other users.
 * @param messageReplyTextStyleMine Sets the style of the text inside the quoted message bubble visible in the input
 * when replying to a message.Applied when replying to messages owned by the currently logged-in user.
 * @param messageReplyMessageBackgroundStrokeColorMine Sets the color of the stroke of the quoted message bubble visible
 * in the input when replying to a message. Applied when replying to messages owned by the currently logged-in user.
 * @param messageReplyMessageBackgroundStrokeWidthMine Sets the width of the stroke of the quoted message bubble visible
 * in the input when replying to a message. Applied when replying to messages owned by the currently logged-in user.
 * @param messageReplyTextStyleTheirs Sets the style of the text inside the quoted message bubble visible in the input
 * when replying to a message.Applied when replying to messages owned by other users.
 * @param messageReplyMessageBackgroundStrokeColorTheirs Sets the color of the stroke of the quoted message bubble
 * visible in the input when replying to a message. Applied when replying to messages owned by other users.
 * @param messageReplyMessageBackgroundStrokeWidthTheirs Sets the width of the stroke of the quoted message bubble
 * visible in the input when replying to a message. Applied when replying to messages owned by other users.
 */
public data class MessageInputViewStyle(
    public val attachButtonEnabled: Boolean,
    public val attachButtonIcon: Drawable,
    public val commandsButtonEnabled: Boolean,
    public val commandsButtonIcon: Drawable,
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
    public val fileCheckboxSelectedDrawable: Drawable,
    public val fileCheckboxDeselectedDrawable: Drawable,
    @ColorInt public val fileCheckboxTextColor: Int,
    public val fileAttachmentEmptyStateTextStyle: TextStyle,
    public val mediaAttachmentEmptyStateTextStyle: TextStyle,
    public val fileAttachmentEmptyStateText: String,
    public val mediaAttachmentEmptyStateText: String,
    public val dismissIconDrawable: Drawable,
    public val cooldownTimerTextStyle: TextStyle,
    public val cooldownTimerBackgroundDrawable: Drawable,
    public val maxAttachmentsCount: Int,
    public val editInputModeIcon: Drawable,
    public val replyInputModeIcon: Drawable,
    @ColorInt public val commandButtonRippleColor: Int?,
    @ColorInt public val attachmentButtonRippleColor: Int?,
    public val messageInputInputType: Int,
    // Message reply customization, by default belongs to center content as well
    @ColorInt public val messageReplyBackgroundColor: Int,
    public val messageReplyTextStyleMine: TextStyle,
    @ColorInt public val messageReplyMessageBackgroundStrokeColorMine: Int,
    @Px public val messageReplyMessageBackgroundStrokeWidthMine: Float,
    public val messageReplyTextStyleTheirs: TextStyle,
    @ColorInt public val messageReplyMessageBackgroundStrokeColorTheirs: Int,
    @Px public val messageReplyMessageBackgroundStrokeWidthTheirs: Float,
) {

    /**
     * Creates an instance of [MessageReplyStyle] from the parameters provided by [MessageInputViewStyle].

     *
     * @return an instance of [MessageReplyStyle].
     */
    public fun toMessageReplyStyle(): MessageReplyStyle = MessageReplyStyle(
        messageBackgroundColorMine = messageReplyBackgroundColor,
        messageBackgroundColorTheirs = messageReplyBackgroundColor,
        linkBackgroundColorMine = messageReplyBackgroundColor,
        linkBackgroundColorTheirs = messageReplyBackgroundColor,
        textStyleMine = messageReplyTextStyleMine,
        textStyleTheirs = messageReplyTextStyleTheirs,
        linkStyleMine = messageReplyTextStyleMine,
        linkStyleTheirs = messageReplyTextStyleTheirs,
        messageStrokeColorMine = messageReplyMessageBackgroundStrokeColorMine,
        messageStrokeColorTheirs = messageReplyMessageBackgroundStrokeColorTheirs,
        messageStrokeWidthMine = messageReplyMessageBackgroundStrokeWidthMine,
        messageStrokeWidthTheirs = messageReplyMessageBackgroundStrokeWidthTheirs,
    )

    public companion object {
        internal operator fun invoke(context: Context, attrs: AttributeSet?): MessageInputViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageInputView,
                R.attr.streamUiMessageInputViewStyle,
                R.style.StreamUi_MessageInputView,
            ).use { a ->
                val attachButtonEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiAttachButtonEnabled,
                    true
                )
                val attachButtonIcon = a.getDrawable(R.styleable.MessageInputView_streamUiAttachButtonIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_attach)!!

                val lightningButtonEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiLightningButtonEnabled,
                    true
                )
                val lightningButtonIcon = a.getDrawable(R.styleable.MessageInputView_streamUiLightningButtonIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command)!!

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

                val sendButtonDisabledIcon = a.getDrawable(R.styleable.MessageInputView_streamUiSendButtonDisabledIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_filled_right_arrow)!!

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
                    AttachmentConstants.MAX_UPLOAD_SIZE_IN_MB
                )

                val maxAttachmentsCount = a.getInt(
                    R.styleable.MessageInputView_streamUiMaxAttachmentsCount,
                    AttachmentConstants.MAX_ATTACHMENTS_COUNT
                )

                val pictureAttachmentIcon = a.getDrawable(
                    R.styleable.MessageInputView_streamUiPictureAttachmentIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!

                val fileAttachmentIcon = a.getDrawable(
                    R.styleable.MessageInputView_streamUiFileAttachmentIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!

                val cameraAttachmentIcon = a.getDrawable(
                    R.styleable.MessageInputView_streamUiCameraAttachmentIcon
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!

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
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_large)
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

                val videoIconDrawable = a.getDrawable(R.styleable.MessageInputView_streamUiAttachmentVideoLogoIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_video)!!

                val videoLengthTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiAttachmentVideoLengthTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesTextColor,
                        context.getColorCompat(R.color.stream_ui_white)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesFontAssets,
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiAttachmentsRecentFilesTextStyle,
                        Typeface.NORMAL
                    )
                    .build()
                val videoLengthVisible =
                    a.getBoolean(R.styleable.MessageInputView_streamUiAttachmentVideoLengthVisible, true)
                val videoIconVisible =
                    a.getBoolean(R.styleable.MessageInputView_streamUiAttachmentVideoIconVisible, true)
                val attachmentSelectionBackgroundColor =
                    a.getColor(
                        R.styleable.MessageInputView_streamUiAttachmentSelectionBackgroundColor,
                        context.getColorCompat(R.color.stream_ui_white_smoke)
                    )
                val attachmentSelectionAttachIcon =
                    a.getDrawable(R.styleable.MessageInputView_streamUiAttachmentSelectionAttachIcon)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_next)!!

                val attachmentDialogTabButtonColorStateList =
                    a.getColorStateList(R.styleable.MessageInputView_streamUiAttachmentTabButtonColorStateList)
                        ?: context.getColorStateListCompat(R.color.stream_ui_attachment_tab_button)

                val mediaAttachmentsTabEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiMediaAttachmentsTabEnabled,
                    true
                )
                val fileAttachmentsTabEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiFileAttachmentsTabEnabled,
                    true
                )
                val cameraAttachmentsTabEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiCameraAttachmentsTabEnabled,
                    true
                )
                val takeImageEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiTakeImageEnabledEnabled,
                    true
                )
                val recordVideoEnabled = a.getBoolean(
                    R.styleable.MessageInputView_streamUiRecordVideoEnabledEnabled,
                    true
                )

                val attachmentDialogStyle = AttachmentSelectionDialogStyle(
                    pictureAttachmentIcon = pictureAttachmentIcon,
                    fileAttachmentIcon = fileAttachmentIcon,
                    cameraAttachmentIcon = cameraAttachmentIcon,
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
                    videoIconDrawable = videoIconDrawable,
                    videoDurationTextStyle = videoLengthTextStyle,
                    videoLengthLabelVisible = videoLengthVisible,
                    videoIconVisible = videoIconVisible,
                    backgroundColor = attachmentSelectionBackgroundColor,
                    attachButtonIcon = attachmentSelectionAttachIcon,
                    toggleButtonColorStateList = attachmentDialogTabButtonColorStateList,
                    mediaAttachmentsTabEnabled = mediaAttachmentsTabEnabled,
                    fileAttachmentsTabEnabled = fileAttachmentsTabEnabled,
                    cameraAttachmentsTabEnabled = cameraAttachmentsTabEnabled,
                    takeImageEnabled = takeImageEnabled,
                    recordVideoEnabled = recordVideoEnabled,
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
                        context.getColorCompat(R.color.stream_ui_literal_white)
                    )

                val fileCheckboxSelectedDrawable =
                    a.getDrawable(R.styleable.MessageInputView_streamUiFileCheckBoxSelectedDrawable)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_circle_blue)!!

                val fileCheckboxDeselectedDrawable =
                    a.getDrawable(R.styleable.MessageInputView_streamUiFileCheckBoxDeselectedDrawable)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

                val fileAttachmentEmptyStateTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiAttachmentsFilesEmptyStateTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiAttachmentsFilesEmptyStateTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiAttachmentsFilesEmptyStateFontAssets,
                        R.styleable.MessageInputView_streamUiAttachmentsFilesEmptyStateFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiAttachmentsFilesEmptyStateStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val mediaAttachmentEmptyStateTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiAttachmentsMediaEmptyStateTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiAttachmentsMediaEmptyStateTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiAttachmentsMediaEmptyStateFontAssets,
                        R.styleable.MessageInputView_streamUiAttachmentsMediaEmptyStateFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiAttachmentsMediaEmptyStateStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val fileAttachmentEmptyStateText =
                    a.getString(R.styleable.MessageInputView_streamUiAttachmentsFilesEmptyStateText)
                        ?: context.getString(R.string.stream_ui_message_input_no_files)

                val mediaAttachmentEmptyStateText =
                    a.getString(R.styleable.MessageInputView_streamUiAttachmentsMediaEmptyStateText)
                        ?: context.getString(R.string.stream_ui_message_input_no_files)

                val dismissIconDrawable =
                    a.getDrawable(R.styleable.MessageInputView_streamUiMessageInputCloseButtonIconDrawable)
                        ?: context.getDrawableCompat(R.drawable.stream_ui_ic_clear)!!

                val cooldownTimerTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCooldownTimerTextSize,
                        context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCooldownTimerTextColor,
                        context.getColorCompat(R.color.stream_ui_literal_white)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiCooldownTimerFontAssets,
                        R.styleable.MessageInputView_streamUiCooldownTimerFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiCooldownTimerTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val cooldownTimerBackgroundDrawable = a.getDrawable(
                    R.styleable.MessageInputView_streamUiCooldownTimerBackgroundDrawable,
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_cooldown_badge_background)!!

                val editInputModeIcon = a.getDrawable(R.styleable.MessageInputView_streamUiEditInputModeIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_edit)!!

                val replyInputModeIcon = a.getDrawable(R.styleable.MessageInputView_streamUiReplyInputModeIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_arrow_curve_left)!!

                val attachmentsButtonRippleColor = a.getColorOrNull(
                    R.styleable.MessageInputView_streamUiAttachButtonRippleColor
                )

                val commandsButtonRippleColor = a.getColorOrNull(
                    R.styleable.MessageInputView_streamUiCommandButtonRippleColor
                )

                val messageInputInputType = a.getInt(
                    R.styleable.MessageComposerView_android_inputType,
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                )

                val mediumTypeface = ResourcesCompat.getFont(context, R.font.stream_roboto_medium) ?: Typeface.DEFAULT

                val messageReplyBackgroundColor: Int =
                    a.getColor(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyBackgroundColor,
                        context.getColorCompat(R.color.stream_ui_white)
                    )

                val messageReplyTextStyleMine: TextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextSizeMine,
                        context.getDimension(MessageReplyStyle.DEFAULT_TEXT_SIZE)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextColorMine,
                        context.getColorCompat(MessageReplyStyle.DEFAULT_TEXT_COLOR)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextFontAssetsMine,
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextStyleMine,
                        mediumTypeface
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextStyleMine,
                        MessageReplyStyle.DEFAULT_TEXT_STYLE
                    )
                    .build()

                val messageReplyMessageBackgroundStrokeColorMine: Int =
                    a.getColor(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyStrokeColorMine,
                        context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                    )

                val messageReplyMessageBackgroundStrokeWidthMine: Float =
                    a.getDimension(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyStrokeWidthMine,
                        DEFAULT_MESSAGE_REPLY_BACKGROUND_STROKE_WIDTH
                    )

                val messageReplyTextStyleTheirs: TextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextSizeTheirs,
                        context.getDimension(MessageReplyStyle.DEFAULT_TEXT_SIZE)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextColorTheirs,
                        context.getColorCompat(MessageReplyStyle.DEFAULT_TEXT_COLOR)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextFontAssetsTheirs,
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextStyleTheirs,
                        mediumTypeface
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyTextStyleTheirs,
                        MessageReplyStyle.DEFAULT_TEXT_STYLE
                    )
                    .build()

                val messageReplyMessageBackgroundStrokeColorTheirs: Int =
                    a.getColor(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyStrokeColorTheirs,
                        context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                    )

                val messageReplyMessageBackgroundStrokeWidthTheirs: Float =
                    a.getDimension(
                        R.styleable.MessageInputView_streamUiMessageInputMessageReplyStrokeWidthTheirs,
                        DEFAULT_MESSAGE_REPLY_BACKGROUND_STROKE_WIDTH
                    )

                return MessageInputViewStyle(
                    attachButtonEnabled = attachButtonEnabled,
                    attachButtonIcon = attachButtonIcon,
                    commandsButtonEnabled = lightningButtonEnabled,
                    commandsButtonIcon = lightningButtonIcon,
                    messageInputTextStyle = messageInputTextStyle,
                    messageInputScrollbarEnabled = messageInputScrollbarEnabled,
                    messageInputScrollbarFadingEnabled = messageInputScrollbarFadingEnabled,
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
                    fileCheckboxSelectedDrawable = fileCheckboxSelectedDrawable,
                    fileCheckboxDeselectedDrawable = fileCheckboxDeselectedDrawable,
                    fileAttachmentEmptyStateTextStyle = fileAttachmentEmptyStateTextStyle,
                    mediaAttachmentEmptyStateTextStyle = mediaAttachmentEmptyStateTextStyle,
                    fileAttachmentEmptyStateText = fileAttachmentEmptyStateText,
                    mediaAttachmentEmptyStateText = mediaAttachmentEmptyStateText,
                    dismissIconDrawable = dismissIconDrawable,
                    cooldownTimerTextStyle = cooldownTimerTextStyle,
                    cooldownTimerBackgroundDrawable = cooldownTimerBackgroundDrawable,
                    maxAttachmentsCount = maxAttachmentsCount,
                    editInputModeIcon = editInputModeIcon,
                    replyInputModeIcon = replyInputModeIcon,
                    attachmentButtonRippleColor = attachmentsButtonRippleColor,
                    commandButtonRippleColor = commandsButtonRippleColor,
                    messageInputInputType = messageInputInputType,
                    messageReplyBackgroundColor = messageReplyBackgroundColor,
                    messageReplyTextStyleMine = messageReplyTextStyleMine,
                    messageReplyMessageBackgroundStrokeColorMine = messageReplyMessageBackgroundStrokeColorMine,
                    messageReplyMessageBackgroundStrokeWidthMine = messageReplyMessageBackgroundStrokeWidthMine,
                    messageReplyTextStyleTheirs = messageReplyTextStyleTheirs,
                    messageReplyMessageBackgroundStrokeColorTheirs = messageReplyMessageBackgroundStrokeColorTheirs,
                    messageReplyMessageBackgroundStrokeWidthTheirs = messageReplyMessageBackgroundStrokeWidthTheirs,
                ).let(TransformStyle.messageInputStyleTransformer::transform)
                    .also { style -> style.checkMaxAttachmentsCountRange() }
            }
        }

        private fun MessageInputViewStyle.checkMaxAttachmentsCountRange() {
            require(maxAttachmentsCount <= AttachmentConstants.MAX_ATTACHMENTS_COUNT) { "maxAttachmentsCount cannot by greater than ${AttachmentConstants.MAX_ATTACHMENTS_COUNT}! Current value: $maxAttachmentsCount" }
        }

        public fun createDefault(context: Context): MessageInputViewStyle = invoke(context, null)

        private const val DEFAULT_MESSAGE_REPLY_BACKGROUND_STROKE_WIDTH = 4F
    }
}
