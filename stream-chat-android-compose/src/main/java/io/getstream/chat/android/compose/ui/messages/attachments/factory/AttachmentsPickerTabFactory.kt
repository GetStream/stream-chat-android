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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

/**
 * Holds the information required to add support for a [AttachmentsPickerMode] mode
 * in the attachment picker.
 */
public interface AttachmentsPickerTabFactory {

    /**
     * The attachment picker mode that this factory handles.
     */
    public val attachmentsPickerMode: AttachmentsPickerMode

    /**
     * Determines if the picker tab is enabled.
     *
     * @return True if the tab is enabled, false otherwise.
     */
    public fun isPickerTabEnabled(channel: Channel): Boolean = true

    /**
     * Icon for the tab.
     *
     * @param isEnabled If the tab is enabled.
     * @param isSelected If the tab is selected.
     */
    @Composable
    public fun PickerTabIcon(isEnabled: Boolean, isSelected: Boolean)

    /**
     * Content for the tab.
     *
     * @param onAttachmentPickerAction A lambda that will be invoked when an action is happened.
     * @param attachments The list of attachments to display.
     * @param onAttachmentsChanged Handler to set the loaded list of attachments to display.
     * @param onAttachmentItemSelected Handler when the item selection state changes.
     * @param onAttachmentsSubmitted Handler to submit the selected attachments to the message composer.
     */
    @Composable
    public fun PickerTabContent(
        onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
        attachments: List<AttachmentPickerItemState>,
        onAttachmentsChanged: (List<AttachmentPickerItemState>) -> Unit,
        onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    )

    /**
     * Content for the fullscreen.
     *
     * @param onAttachmentPickerAction A lambda that will be invoked when an action is happened.
     */
    @Composable
    public fun PickerFullscreenContent(
        onAttachmentPickerAction: (AttachmentPickerAction) -> Unit,
    ) {
    }
}
