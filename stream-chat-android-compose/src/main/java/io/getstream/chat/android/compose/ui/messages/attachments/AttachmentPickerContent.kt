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
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CommandPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

@Suppress("LongParameterList")
@Composable
internal fun AttachmentPickerContent(
    pickerMode: AttachmentPickerMode?,
    commands: List<Command>,
    attachments: List<AttachmentPickerItemState>,
    onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
    actions: AttachmentPickerActions,
    onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
) {
    when (pickerMode) {
        is GalleryPickerMode -> ChatTheme.componentFactory.AttachmentMediaPicker(
            pickerMode = pickerMode,
            attachments = attachments,
            onAttachmentsChanged = onAttachmentsChanged,
            onAttachmentItemSelected = actions.onAttachmentItemSelected,
        )

        is FilePickerMode -> ChatTheme.componentFactory.AttachmentFilePicker(
            pickerMode = pickerMode,
            attachments = attachments,
            onAttachmentsChanged = onAttachmentsChanged,
            onAttachmentItemSelected = actions.onAttachmentItemSelected,
            onAttachmentsSubmitted = onAttachmentsSubmitted,
        )

        is CameraPickerMode -> ChatTheme.componentFactory.AttachmentCameraPicker(
            pickerMode = pickerMode,
            onAttachmentsSubmitted = onAttachmentsSubmitted,
        )

        is PollPickerMode -> ChatTheme.componentFactory.AttachmentPollPicker(
            pickerMode = pickerMode,
            onCreatePollClick = actions.onCreatePollClick,
            onCreatePoll = actions.onCreatePoll,
            onCreatePollDismissed = actions.onCreatePollDismissed,
        )

        is CommandPickerMode -> ChatTheme.componentFactory.AttachmentCommandPicker(
            pickerMode = pickerMode,
            commands = commands,
            onCommandSelected = actions.onCommandSelected,
        )

        // Custom modes are handled by overriding AttachmentPickerContent in ChatComponentFactory
        else -> Unit
    }
}
