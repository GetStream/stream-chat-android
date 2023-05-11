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
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorStateListCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat

/**
 * Style for [AttachmentSelectionDialogFragment].
 *
 * @param attachmentsPickerBackgroundColor The background color of the picker.
 * @param allowAccessButtonTextStyle The text style used for all the buttons used to request required permissions.
 * @param submitAttachmentsButtonIconDrawable The icon for the submit selected attachments button.
 * @param attachmentTabToggleButtonStateList The color selector that will be applied to each tab button.
 * @param mediaAttachmentsTabEnabled If the media attachments tab is displayed in the picker.
 * @param mediaAttachmentsTabIconDrawable The icon for the image attachments tab.
 * @param allowAccessToMediaButtonText The text to request required permissions on the image attachments tab.
 * @param allowAccessToMediaIconDrawable The icon above the permissions text on the image attachments tab.
 * @param videoLengthTextVisible If the video duration caption is displayed.
 * @param videoLengthTextStyle The text style used for video duration caption in each tile.
 * @param videoIconVisible If the video icon is displayed.
 * @param videoIconDrawable The icon used for video files.
 * @param mediaAttachmentNoMediaText The text that will be displayed if there are no media files on the device.
 * @param mediaAttachmentNoMediaTextStyle The text style for the the no media files text.
 * @param fileAttachmentsTabEnabled If the file attachments tab is displayed in the picker..
 * @param fileAttachmentsTabIconDrawable The icon for the file attachments tab.
 * @param allowAccessToFilesButtonText The text to request required permissions on the file attachments tab.
 * @param allowAccessToFilesIconDrawable The icon above the permissions text on the file attachments tab.
 * @param recentFilesText The text above the file list on the file attachments tab.
 * @param recentFilesTextStyle The text style of the recent files text on the file attachments tab.
 * @param fileManagerIconDrawable The icon for a button to pick files from file manager.
 * @param fileAttachmentsNoFilesText The text that will be displayed if there are no files on the device.
 * @param fileAttachmentsNoFilesTextStyle The text style for the the no files text.
 * @param fileAttachmentItemNameTextStyle The text style for the file name in a file attachment item.
 * @param fileAttachmentItemSizeTextStyle The text style for the file size in a file attachment item.
 * @param fileAttachmentItemCheckboxSelectedDrawable The drawable for the selected state of the checkbox in file attachment item.
 * @param fileAttachmentItemCheckboxDeselectedDrawable The drawable for the deselected state of the checkbox in file attachment item.
 * @param fileAttachmentItemCheckboxTextColor The color of the checkbox  in file attachment item.
 * @param cameraAttachmentsTabEnabled If the media capture tab is displayed in the picker.
 * @param cameraAttachmentsTabIconDrawable The icon for the camera attachments tab.
 * @param allowAccessToCameraButtonText The text to request required permissions on the camera attachments tab.
 * @param allowAccessToCameraIconDrawable The icon above the permissions text on the camera attachments tab.
 * @param takeImageEnabled If starting image capture is enabled.
 * @param recordVideoEnabled If starting video capture is enabled.
 */
@ExperimentalStreamChatApi
public data class AttachmentsPickerDialogStyle(
    @ColorInt val attachmentsPickerBackgroundColor: Int,
    val allowAccessButtonTextStyle: TextStyle,
    val submitAttachmentsButtonIconDrawable: Drawable,
    val attachmentTabToggleButtonStateList: ColorStateList?,
    // Media attachments tab
    val mediaAttachmentsTabEnabled: Boolean,
    val mediaAttachmentsTabIconDrawable: Drawable,
    val allowAccessToMediaButtonText: String,
    val allowAccessToMediaIconDrawable: Drawable,
    val videoLengthTextVisible: Boolean,
    val videoLengthTextStyle: TextStyle,
    val videoIconVisible: Boolean,
    val videoIconDrawable: Drawable,
    val mediaAttachmentNoMediaText: String,
    val mediaAttachmentNoMediaTextStyle: TextStyle,
    // File attachments tab
    val fileAttachmentsTabEnabled: Boolean,
    val fileAttachmentsTabIconDrawable: Drawable,
    val allowAccessToFilesButtonText: String,
    val allowAccessToFilesIconDrawable: Drawable,
    val recentFilesText: String,
    val recentFilesTextStyle: TextStyle,
    val fileManagerIconDrawable: Drawable,
    val fileAttachmentsNoFilesText: String,
    val fileAttachmentsNoFilesTextStyle: TextStyle,
    val fileAttachmentItemNameTextStyle: TextStyle,
    val fileAttachmentItemSizeTextStyle: TextStyle,
    val fileAttachmentItemCheckboxSelectedDrawable: Drawable,
    val fileAttachmentItemCheckboxDeselectedDrawable: Drawable,
    @ColorInt val fileAttachmentItemCheckboxTextColor: Int,
    // Camera attachments tab
    val cameraAttachmentsTabEnabled: Boolean,
    val cameraAttachmentsTabIconDrawable: Drawable,
    val allowAccessToCameraButtonText: String,
    val allowAccessToCameraIconDrawable: Drawable,
    val takeImageEnabled: Boolean,
    val recordVideoEnabled: Boolean,
) {
    public companion object {
        internal operator fun invoke(context: Context, attrs: AttributeSet?): AttachmentsPickerDialogStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.AttachmentsPickerDialog,
                R.attr.streamUiAttachmentsPickerDialogStyle,
                R.style.StreamUi_AttachmentsPickerDialog,
            ).use { a ->
                val attachmentsPickerBackgroundColor = a.getColor(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white_smoke)
                )

                val allowAccessButtonTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessButtonTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessButtonTextColor,
                        context.getColorCompat(R.color.stream_ui_accent_blue)
                    )
                    .font(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessButtonTextFontAssets,
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessButtonTextFont
                    )
                    .style(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessButtonTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val submitAttachmentsButtonIconDrawable = a.getDrawableCompat(
                    context,
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerSubmitAttachmentsButtonIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_next)!!

                val attachmentTabToggleButtonStateList = a.getColorStateList(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAttachmentTabToggleButtonStateList
                ) ?: context.getColorStateListCompat(R.color.stream_ui_attachment_tab_button)

                /**
                 * Media attachments tab
                 */
                val mediaAttachmentsTabEnabled = a.getBoolean(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerMediaAttachmentsTabEnabled,
                    true
                )

                val mediaAttachmentsTabIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerMediaAttachmentsTabIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!

                val allowAccessToMediaButtonText = a.getText(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessToMediaButtonText
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_gallery_access)

                val allowAccessToMediaIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessToMediaIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_media)!!

                val videoLengthTextVisible = a.getBoolean(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerVideoLengthTextVisible,
                    true
                )

                val videoLengthTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerVideoLengthTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerVideoLengthTextColor,
                        context.getColorCompat(R.color.stream_ui_white)
                    )
                    .font(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerVideoLengthTextFontAssets,
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerVideoLengthTextFont
                    )
                    .style(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerVideoLengthTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val videoIconVisible = a.getBoolean(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerVideoIconVisible,
                    true
                )

                val videoIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerVideoIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_video)!!

                val mediaAttachmentNoMediaText = a.getString(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerMediaAttachmentNoMediaText
                ) ?: context.getString(R.string.stream_ui_message_composer_no_files)

                val mediaAttachmentNoMediaTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerMediaAttachmentNoMediaTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerMediaAttachmentNoMediaTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerMediaAttachmentNoMediaTextFontAssets,
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerMediaAttachmentNoMediaTextFont
                    )
                    .style(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerMediaAttachmentNoMediaTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                /**
                 * File attachments tab
                 */
                val fileAttachmentsTabEnabled = a.getBoolean(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsTabEnabled,
                    true
                )

                val fileAttachmentsTabIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsTabIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!

                val allowAccessToFilesButtonText = a.getText(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessToFilesButtonText
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_files_access)

                val allowAccessToFilesIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessToFilesIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_file)!!

                val recentFilesText = a.getText(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerRecentFilesText
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_recent_files)

                val recentFilesTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerRecentFilesTextSize,
                        context.getDimension(R.dimen.stream_ui_spacing_medium)
                    )
                    .color(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerRecentFilesTextColor,
                        context.getColorCompat(R.color.stream_ui_black)
                    )
                    .font(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerRecentFilesTextFontAssets,
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerRecentFilesTextFont
                    )
                    .style(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerRecentFilesTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val fileManagerIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileManagerIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

                val fileAttachmentsNoFilesText = a.getString(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsNoFilesText
                ) ?: context.getString(R.string.stream_ui_message_composer_no_files)

                val fileAttachmentsNoFilesTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsNoFilesTextSize,
                        context.getDimension(R.dimen.stream_ui_text_large)
                    )
                    .color(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsNoFilesTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsNoFilesTextFontAssets,
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsNoFilesTextFont
                    )
                    .style(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentsNoFilesTextStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val fileAttachmentItemNameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemNameTextColor,
                        context.getColorCompat(R.color.stream_ui_black)
                    )
                    .font(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemNameTextFontAssets,
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemNameTextFont
                    )
                    .style(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemNameTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val fileAttachmentItemSizeTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemSizeTextSize,
                        context.getDimension(R.dimen.stream_ui_text_small)
                    )
                    .color(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemSizeTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemSizeTextFontAssets,
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemSizeTextFont
                    )
                    .style(
                        R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemSizeTextStyle,
                        Typeface.BOLD
                    )
                    .build()

                val fileAttachmentItemCheckboxSelectedDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemCheckboxSelectedDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_circle_blue)!!

                val fileAttachmentItemCheckboxDeselectedDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemCheckboxDeselectedDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_ic_file_manager)!!

                val fileAttachmentItemCheckboxTextColor = a.getColor(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerFileAttachmentItemCheckboxTextColor,
                    context.getColorCompat(R.color.stream_ui_literal_white)
                )

                /**
                 * Camera attachments tab
                 */
                val cameraAttachmentsTabEnabled = a.getBoolean(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerCameraAttachmentsTabEnabled,
                    true
                )
                val takeImageEnabled = a.getBoolean(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerTakeImageEnabled,
                    true
                )
                val recordVideoEnabled = a.getBoolean(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerRecordVideoEnabled,
                    true
                )

                val cameraAttachmentsTabIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerCameraAttachmentsTabIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!

                val allowAccessToCameraButtonText = a.getText(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessToCameraButtonText
                )?.toString() ?: context.getString(R.string.stream_ui_message_composer_camera_access)

                val allowAccessToCameraIconDrawable = a.getDrawable(
                    R.styleable.AttachmentsPickerDialog_streamUiAttachmentsPickerAllowAccessToCameraIconDrawable
                ) ?: context.getDrawableCompat(R.drawable.stream_ui_attachment_permission_camera)!!

                return AttachmentsPickerDialogStyle(
                    attachmentsPickerBackgroundColor = attachmentsPickerBackgroundColor,
                    allowAccessButtonTextStyle = allowAccessButtonTextStyle,
                    submitAttachmentsButtonIconDrawable = submitAttachmentsButtonIconDrawable,
                    attachmentTabToggleButtonStateList = attachmentTabToggleButtonStateList,
                    // Media attachments tab
                    mediaAttachmentsTabEnabled = mediaAttachmentsTabEnabled,
                    mediaAttachmentsTabIconDrawable = mediaAttachmentsTabIconDrawable,
                    allowAccessToMediaButtonText = allowAccessToMediaButtonText,
                    allowAccessToMediaIconDrawable = allowAccessToMediaIconDrawable,
                    videoLengthTextVisible = videoLengthTextVisible,
                    videoLengthTextStyle = videoLengthTextStyle,
                    videoIconVisible = videoIconVisible,
                    videoIconDrawable = videoIconDrawable,
                    mediaAttachmentNoMediaText = mediaAttachmentNoMediaText,
                    mediaAttachmentNoMediaTextStyle = mediaAttachmentNoMediaTextStyle,
                    // File attachments tab
                    fileAttachmentsTabEnabled = fileAttachmentsTabEnabled,
                    fileAttachmentsTabIconDrawable = fileAttachmentsTabIconDrawable,
                    allowAccessToFilesButtonText = allowAccessToFilesButtonText,
                    allowAccessToFilesIconDrawable = allowAccessToFilesIconDrawable,
                    recentFilesText = recentFilesText,
                    recentFilesTextStyle = recentFilesTextStyle,
                    fileManagerIconDrawable = fileManagerIconDrawable,
                    fileAttachmentsNoFilesText = fileAttachmentsNoFilesText,
                    fileAttachmentsNoFilesTextStyle = fileAttachmentsNoFilesTextStyle,
                    fileAttachmentItemNameTextStyle = fileAttachmentItemNameTextStyle,
                    fileAttachmentItemSizeTextStyle = fileAttachmentItemSizeTextStyle,
                    fileAttachmentItemCheckboxSelectedDrawable = fileAttachmentItemCheckboxSelectedDrawable,
                    fileAttachmentItemCheckboxDeselectedDrawable = fileAttachmentItemCheckboxDeselectedDrawable,
                    fileAttachmentItemCheckboxTextColor = fileAttachmentItemCheckboxTextColor,
                    // Camera attachments tab
                    cameraAttachmentsTabEnabled = cameraAttachmentsTabEnabled,
                    cameraAttachmentsTabIconDrawable = cameraAttachmentsTabIconDrawable,
                    allowAccessToCameraButtonText = allowAccessToCameraButtonText,
                    allowAccessToCameraIconDrawable = allowAccessToCameraIconDrawable,
                    takeImageEnabled = takeImageEnabled,
                    recordVideoEnabled = recordVideoEnabled,
                ).let(TransformStyle.attachmentsPickerStyleTransformer::transform)
            }
        }
    }
}
