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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.MessageMode

/**
 * The main attachment picker component that allows users to select and attach files to messages.
 *
 * This composable displays tabs for different attachment types (gallery, files, camera, polls, commands)
 * and the corresponding content for the selected tab. It is typically wrapped by [AttachmentPickerMenu]
 * which handles the animated expand/collapse behavior and positioning.
 *
 * The picker behavior is configured through [ChatTheme.attachmentPickerConfig]:
 * - When `useSystemPicker` is `true`, shows buttons that launch system pickers (no permissions required)
 * - When `useSystemPicker` is `false`, shows an in-app grid picker (requires storage permissions)
 *
 * The picker integrates with [AttachmentsPickerViewModel] to manage state and with the message composer
 * to add selected attachments to messages.
 *
 * For customization, override the picker components in [ChatTheme.componentFactory]:
 * - `AttachmentTypePicker` - The tab bar for switching between modes
 * - `AttachmentPickerContent` - The content area for each mode
 * - `AttachmentSystemPicker` - The system picker variant
 *
 * @param attachmentsPickerViewModel The [AttachmentsPickerViewModel] that manages picker state,
 * including the current mode, available attachments, and selection state.
 * @param messageMode The current message mode ([MessageMode.Normal] or [MessageMode.MessageThread]).
 * Used to determine if poll creation is available (not available in threads).
 * @param onAttachmentItemSelected Called when a user taps an attachment item to select/deselect it.
 * By default, delegates to [AttachmentsPickerViewModel.changeSelectedAttachments].
 * @param onAttachmentsSelected Called when attachments are confirmed and should be added to the composer.
 * Receives the list of [Attachment] objects ready to be sent.
 * @param onAttachmentPickerAction Called for picker-specific actions like poll creation or command selection.
 * See [AttachmentPickerAction] for available actions.
 * @param onDismiss Called when the picker should be dismissed (back press, outside tap, etc.).
 */
@Suppress("LongMethod")
@Composable
public fun AttachmentPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    messageMode: MessageMode = MessageMode.Normal,
    onAttachmentItemSelected: (AttachmentPickerItemState) -> Unit =
        attachmentsPickerViewModel::changeSelectedAttachments,
    onAttachmentsSelected: (List<Attachment>) -> Unit = {
        attachmentsPickerViewModel.changeAttachmentState(showAttachments = false)
    },
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit = {},
    onDismiss: () -> Unit = { attachmentsPickerViewModel.changeAttachmentState(showAttachments = false) },
) {
    val saveAttachmentsOnDismiss = ChatTheme.attachmentPickerTheme.saveAttachmentsOnDismiss
    val dismissAction = {
        if (saveAttachmentsOnDismiss) {
            attachmentsPickerViewModel.getSelectedAttachmentsAsync { attachments ->
                onAttachmentsSelected(attachments)
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }
    BackHandler(onBack = dismissAction)

    Surface(
        modifier = Modifier.testTag("Stream_AttachmentsPicker"),
        color = ChatTheme.colors.barsBackground,
    ) {
        if (ChatTheme.attachmentPickerConfig.useSystemPicker) {
            ChatTheme.componentFactory.AttachmentSystemPicker(
                channel = attachmentsPickerViewModel.channel,
                messageMode = messageMode,
                commands = attachmentsPickerViewModel.channel.config.commands,
                attachments = attachmentsPickerViewModel.attachments,
                onAttachmentPickerAction = onAttachmentPickerAction,
                onAttachmentsSubmitted = { metaData ->
                    attachmentsPickerViewModel.getAttachmentsFromMetadataAsync(metaData) { attachments ->
                        onAttachmentsSelected(attachments)
                    }
                },
            )
        } else {
            Column {
                ChatTheme.componentFactory.AttachmentTypePicker(
                    channel = attachmentsPickerViewModel.channel,
                    messageMode = messageMode,
                    selectedAttachmentPickerMode = attachmentsPickerViewModel.pickerMode,
                    onModeSelected = attachmentsPickerViewModel::changePickerMode,
                    additionalContent = {},
                )
                AnimatedContent(
                    targetState = attachmentsPickerViewModel.pickerMode,
                ) { pickerMode ->
                    ChatTheme.componentFactory.AttachmentPickerContent(
                        pickerMode = pickerMode,
                        commands = attachmentsPickerViewModel.channel.config.commands,
                        attachments = attachmentsPickerViewModel.attachments,
                        onAttachmentsChanged = { attachmentsPickerViewModel.attachments = it },
                        onAttachmentItemSelected = onAttachmentItemSelected,
                        onAttachmentPickerAction = onAttachmentPickerAction,
                        onAttachmentsSubmitted = { metaDataList ->
                            attachmentsPickerViewModel.getAttachmentsFromMetadataAsync(metaDataList) { attachments ->
                                onAttachmentsSelected(attachments)
                            }
                        },
                    )
                }
            }
        }
    }
}
