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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.helper.ViewStyle

/**
 * Style for [AttachmentsPickerDialogFragment].
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
 * @param videoIconDrawableTint The tint of the icon used for video files.
 * @param mediaAttachmentNoMediaText The text that will be displayed if there are no media files on the device.
 * @param mediaAttachmentNoMediaTextStyle The text style for the the no media files text.
 * @param fileAttachmentsTabEnabled If the file attachments tab is displayed in the picker.
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
 * @param pickerMediaMode define which media type will be allowed.
 */
public data class AttachmentsPickerDialogStyle(
    @ColorInt val attachmentsPickerBackgroundColor: Int,
    val allowAccessButtonTextStyle: TextStyle,
    // Dialog header section
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
    @ColorInt val videoIconDrawableTint: Int?,
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
    val pickerMediaMode: PickerMediaMode,
) : ViewStyle
