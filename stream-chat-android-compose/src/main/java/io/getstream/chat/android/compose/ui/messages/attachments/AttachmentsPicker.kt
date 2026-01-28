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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentsPickerMode
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentPickerAction
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Represents the bottom bar UI that allows users to pick attachments. The picker renders its
 * tabs based on the [tabFactories] parameter. Out of the box we provide factories for images,
 * files and media capture tabs.
 *
 * @param attachmentsPickerViewModel ViewModel that loads the images or files and persists which
 * @param modifier Modifier for styling.
 * @param shape The shape of the bottom bar.
 * @param messageMode The message mode, used to determine if the default "Polls" tab is enabled.
 * items have been selected.
 * @param tabFactories The list of attachment picker tab factories.
 * @param onAttachmentsSelected Handler when attachments are selected and confirmed by the user.
 * @param onAttachmentPickerAction A lambda that will be invoked when an action is happened.
 * @param onDismiss Handler when the user dismisses the UI.
 */
@Composable
public fun AttachmentsPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    messageMode: MessageMode = MessageMode.Normal,
    tabFactories: List<AttachmentsPickerTabFactory> = ChatTheme.attachmentsPickerTabFactories,
    onTabClick: (Int, AttachmentsPickerMode) -> Unit = { _, _ -> },
    onAttachmentsSelected: (List<Attachment>) -> Unit = {},
    onAttachmentPickerAction: (AttachmentPickerAction) -> Unit = {},
    onDismiss: () -> Unit = {
        attachmentsPickerViewModel.changeAttachmentState(false)
        attachmentsPickerViewModel.dismissAttachments()
    },
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
    val channel = attachmentsPickerViewModel.channel

    Surface(
        modifier = modifier.testTag("Stream_AttachmentsPicker"),
        shape = shape,
        color = ChatTheme.colors.barsBackground,
    ) {
        Column {
            AttachmentTypePicker(
                messageMode = messageMode,
                channel = channel,
                onPickerTypeClick = { index, attachmentPickerMode ->
                    onTabClick(index, attachmentPickerMode)
                    attachmentsPickerViewModel.changeAttachmentPickerMode(attachmentPickerMode)
                },
            ) { attachmentsPickerMode ->
                AnimatedContent(
                    targetState = attachmentsPickerMode,
                ) { pickerMode ->
                    AttachmentPickerContent(
                        attachmentsPickerMode = pickerMode,
                        attachments = attachmentsPickerViewModel.attachments,
                        onAttachmentsChanged = { attachmentsPickerViewModel.attachments = it },
                        onAttachmentItemSelected = attachmentsPickerViewModel::changeSelectedAttachments,
                        onAttachmentsSubmitted = { metaData ->
                            attachmentsPickerViewModel.getAttachmentsFromMetadataAsync(metaData) { attachments ->
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

@Preview
@Composable
private fun AttachmentsPickerPreview() {
    ChatPreviewTheme {
        AttachmentsPicker(
            attachmentsPickerViewModel = AttachmentsPickerViewModel(
                storageHelper = StorageHelperWrapper(LocalContext.current),
                channelState = MutableStateFlow(null),
            ),
        )
    }
}
