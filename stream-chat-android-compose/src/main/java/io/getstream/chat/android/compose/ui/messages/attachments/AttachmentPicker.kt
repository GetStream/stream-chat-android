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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.MessageMode

/**
 * The main attachment picker bottom bar that allows users to select attachments.
 * Displays tabs for different attachment types and handles attachment selection.
 *
 * @param attachmentsPickerViewModel The view model that manages the picker state.
 * @param messageMode The current message mode (Normal or Thread).
 * @param onAttachmentItemSelected Handler invoked when an attachment item is selected.
 * @param onAttachmentsSelected Handler invoked when attachments are confirmed.
 * @param onAttachmentPickerAction Handler invoked for picker actions.
 * @param onDismiss Handler invoked when the picker is dismissed.
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

/**
 * The default "Send" button in the attachments picker heading.
 *
 * @param hasPickedAttachments Indicator if there are selected attachments.
 * @param onClick The action to be taken when the button is clicked.
 */
@Composable
internal fun DefaultAttachmentsPickerSendButton(
    hasPickedAttachments: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        enabled = hasPickedAttachments,
        onClick = onClick,
        content = {
            val layoutDirection = LocalLayoutDirection.current
            Icon(
                modifier = Modifier
                    .mirrorRtl(layoutDirection = layoutDirection)
                    .testTag("Stream_AttachmentPickerSendButton"),
                painter = painterResource(id = R.drawable.stream_compose_ic_left),
                contentDescription = stringResource(id = R.string.stream_compose_send_attachment),
                tint = if (hasPickedAttachments) {
                    ChatTheme.colors.primaryAccent
                } else {
                    ChatTheme.colors.textLowEmphasis
                },
            )
        },
    )
}
