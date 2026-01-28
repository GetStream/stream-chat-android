/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.attachments

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CustomPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.Files
import io.getstream.chat.android.compose.state.messages.attachments.Images
import io.getstream.chat.android.compose.state.messages.attachments.MediaCapture
import io.getstream.chat.android.compose.state.messages.attachments.Poll
import io.getstream.chat.android.compose.state.messages.attachments.System
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

@Composable
internal fun AttachmentPickerContent(
    attachmentsPickerMode: AttachmentsPickerMode,
    attachments: List<AttachmentPickerItemState>,
    onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
    onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
    onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
) {
    when (attachmentsPickerMode) {
        is Images -> AttachmentMediaPicker(
            attachments = attachments,
            onAttachmentsChanged = onAttachmentsChanged,
            onAttachmentItemSelected = onAttachmentItemSelected,
        )

        is Files -> AttachmentFilePicker(
            attachments = attachments,
            onAttachmentsChanged = onAttachmentsChanged,
            onAttachmentItemSelected = onAttachmentItemSelected,
            onAttachmentsSubmitted = onAttachmentsSubmitted,
        )

        is MediaCapture -> AttachmentCameraPicker(
            pickerMediaMode = PickerMediaMode.PHOTO_AND_VIDEO,
            onAttachmentsSubmitted = onAttachmentsSubmitted,
        )

        is Poll -> AttachmentPollPicker(
            onClick = { /* Navigate to create poll screen */ },
        )

        is System -> TODO()
        is CustomPickerMode -> TODO()
    }
}
