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

package io.getstream.chat.android.ui.message.composer.internal

import android.content.Context
import io.getstream.chat.android.ui.message.composer.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.message.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogStyle

/**
 * A mapper to convert [AttachmentsPickerDialogStyle] into the legacy style object that
 * can be used with [AttachmentSelectionDialogFragment].
 *
 * @param context The context to load string resources.
 */
internal fun AttachmentsPickerDialogStyle.toMessageInputViewStyle(
    context: Context,
    style: MessageComposerViewStyle,
): MessageInputViewStyle {
    return MessageInputViewStyle.createDefault(context).copy(
        attachmentSelectionDialogStyle = AttachmentSelectionDialogStyle.createDefault(context).copy(
            backgroundColor = attachmentsPickerBackgroundColor,
            grantPermissionsTextStyle = allowAccessButtonTextStyle,
            attachButtonIcon = submitAttachmentsButtonIconDrawable,
            toggleButtonColorStateList = attachmentTabToggleButtonStateList,
            // Media attachments tab
            mediaAttachmentsTabEnabled = mediaAttachmentsTabEnabled,
            pictureAttachmentIcon = mediaAttachmentsTabIconDrawable,
            allowAccessToGalleryText = allowAccessToMediaButtonText,
            allowAccessToGalleryIcon = allowAccessToMediaIconDrawable,
            videoLengthLabelVisible = videoLengthTextVisible,
            videoDurationTextStyle = videoLengthTextStyle,
            videoIconVisible = videoIconVisible,
            videoIconDrawable = videoIconDrawable,
            // File attachments tab
            fileAttachmentsTabEnabled = fileAttachmentsTabEnabled,
            fileAttachmentIcon = fileAttachmentsTabIconDrawable,
            allowAccessToFilesText = allowAccessToFilesButtonText,
            allowAccessToFilesIcon = allowAccessToFilesIconDrawable,
            recentFilesText = recentFilesText,
            recentFilesTextStyle = recentFilesTextStyle,
            fileManagerIcon = fileManagerIconDrawable,
            // Camera attachments tab
            cameraAttachmentsTabEnabled = cameraAttachmentsTabEnabled,
            cameraAttachmentIcon = cameraAttachmentsTabIconDrawable,
            allowAccessToCameraText = allowAccessToCameraButtonText,
            allowAccessToCameraIcon = allowAccessToCameraIconDrawable,
            takeImageEnabled = style.takeImageEnabled,
            recordVideoEnabled = style.recordVideoEnabled,
        ),
        // Media attachments tab
        mediaAttachmentEmptyStateText = mediaAttachmentNoMediaText,
        mediaAttachmentEmptyStateTextStyle = mediaAttachmentNoMediaTextStyle,
        // File attachments tab
        fileAttachmentEmptyStateText = fileAttachmentsNoFilesText,
        fileAttachmentEmptyStateTextStyle = fileAttachmentsNoFilesTextStyle,
        fileNameTextStyle = fileAttachmentItemNameTextStyle,
        fileSizeTextStyle = fileAttachmentItemSizeTextStyle,
        fileCheckboxSelectedDrawable = fileAttachmentItemCheckboxSelectedDrawable,
        fileCheckboxDeselectedDrawable = fileAttachmentItemCheckboxDeselectedDrawable,
        fileCheckboxTextColor = fileAttachmentItemCheckboxTextColor,
    )
}
