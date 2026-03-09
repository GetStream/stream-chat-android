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

import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.PollConfig

/**
 * Actions that can be performed in the attachment picker.
 *
 * Each property maps a user gesture or lifecycle event to a handler.
 * Override individual actions via [copy] to customise behaviour while keeping the rest at their defaults.
 *
 * @property onAttachmentItemSelected Called when a user taps an attachment item to select or deselect it
 *  inside the in-app picker grid.
 * @property onAttachmentsSelected Called when attachments are confirmed and should be added to the composer.
 *  Receives the list of [Attachment] objects ready to be sent. Triggered by system pickers, camera, and
 *  file browser results.
 * @property onCreatePollClick Called when the user taps the "Create Poll" button, opening the poll
 *  creation dialog.
 * @property onCreatePoll Called when the user submits a new poll configuration from the creation screen.
 * @property onCreatePollDismissed Called when the poll creation dialog is closed without submitting
 *  (e.g. back press or discard).
 * @property onCommandSelected Called when the user selects a slash command from the command picker.
 * @property onDismiss Called when the attachment picker should be dismissed (back press, outside tap, etc.).
 */
public data class AttachmentPickerActions(
    val onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit,
    val onAttachmentsSelected: (List<Attachment>) -> Unit,
    val onCreatePollClick: () -> Unit,
    val onCreatePoll: (PollConfig) -> Unit,
    val onCreatePollDismissed: () -> Unit,
    val onCommandSelected: (Command) -> Unit,
    val onDismiss: () -> Unit,
) {
    public companion object {

        /**
         * No-op implementation of [AttachmentPickerActions].
         */
        public val None: AttachmentPickerActions = AttachmentPickerActions(
            onAttachmentItemSelected = {},
            onAttachmentsSelected = {},
            onCreatePollClick = {},
            onCreatePoll = {},
            onCreatePollDismissed = {},
            onCommandSelected = {},
            onDismiss = {},
        )

        /**
         * Lightweight defaults suitable for standalone [AttachmentPicker] usage without a composer.
         *
         * Handles picker-level concerns only: toggling the grid selection index and dismissing the
         * picker. Attachment submission, poll, and command actions are no-ops.
         *
         * @param attachmentsPickerViewModel The [AttachmentsPickerViewModel] that drives picker state.
         */
        public fun pickerDefaults(
            attachmentsPickerViewModel: AttachmentsPickerViewModel,
        ): AttachmentPickerActions = AttachmentPickerActions(
            onAttachmentItemSelected = { item ->
                val uriString = item.attachmentMetaData.uri?.toString() ?: return@AttachmentPickerActions
                val multiSelect = attachmentsPickerViewModel.pickerMode?.allowMultipleSelection == true
                if (item.isSelected) {
                    attachmentsPickerViewModel.removeFromGridSelection(uriString)
                } else {
                    if (!multiSelect) attachmentsPickerViewModel.clearGridSelection()
                    attachmentsPickerViewModel.addToGridSelection(uriString)
                }
            },
            onAttachmentsSelected = {},
            onCreatePollClick = {},
            onCreatePoll = {},
            onCreatePollDismissed = {},
            onCommandSelected = {},
            onDismiss = { attachmentsPickerViewModel.setPickerVisible(visible = false) },
        )

        /**
         * Default implementation wiring both the picker and composer view models.
         *
         * [AttachmentsPickerViewModel] owns the grid selection index (URI checkmarks).
         * [MessageComposerViewModel] owns the full attachment list for the message.
         * This function is the sole coordination point between the two.
         *
         * @param attachmentsPickerViewModel The [AttachmentsPickerViewModel] that drives picker state.
         * @param composerViewModel The [MessageComposerViewModel] that owns the selected attachment list.
         */
        public fun defaultActions(
            attachmentsPickerViewModel: AttachmentsPickerViewModel,
            composerViewModel: MessageComposerViewModel,
        ): AttachmentPickerActions = AttachmentPickerActions(
            onAttachmentItemSelected = { item ->
                val uriString = item.attachmentMetaData.uri?.toString() ?: return@AttachmentPickerActions
                val multiSelect = attachmentsPickerViewModel.pickerMode?.allowMultipleSelection == true
                if (item.isSelected) {
                    attachmentsPickerViewModel.removeFromGridSelection(uriString)
                    composerViewModel.removeAttachmentsByUris(setOf(uriString))
                } else {
                    val attachment = attachmentsPickerViewModel
                        .getAttachmentsFromMetadata(listOf(item.attachmentMetaData))
                        .firstOrNull() ?: return@AttachmentPickerActions
                    if (!multiSelect) {
                        composerViewModel.removeAttachmentsByUris(attachmentsPickerViewModel.gridSelectedUris.value)
                        attachmentsPickerViewModel.clearGridSelection()
                    }
                    attachmentsPickerViewModel.addToGridSelection(uriString)
                    composerViewModel.addAttachments(listOf(attachment))
                }
            },
            onAttachmentsSelected = { attachments ->
                composerViewModel.addAttachments(attachments)
            },
            onCreatePollClick = {},
            onCreatePoll = { pollConfig ->
                attachmentsPickerViewModel.setPickerVisible(visible = false)
                attachmentsPickerViewModel.clearGridSelection()
                composerViewModel.clearAttachments()
                composerViewModel.createPoll(pollConfig)
            },
            onCreatePollDismissed = {},
            onCommandSelected = { command ->
                attachmentsPickerViewModel.setPickerVisible(visible = false)
                attachmentsPickerViewModel.clearGridSelection()
                composerViewModel.clearAttachments()
                composerViewModel.selectCommand(command)
            },
            onDismiss = { attachmentsPickerViewModel.setPickerVisible(visible = false) },
        )
    }
}
