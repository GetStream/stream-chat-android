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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

/**
 * Parameters for the [ChatComponentFactory.MessageReactions] component.
 *
 * @param modifier Modifier for styling.
 * @param message The message for which the reactions are displayed.
 * @param reactions The list of reaction options to display.
 * @param onClick Handler when the reaction list is clicked. The message is provided as a parameter.
 */
public data class MessageReactionsParams(
    val modifier: Modifier = Modifier,
    val message: Message,
    val reactions: List<MessageReactionItemState>,
    val onClick: ((message: Message) -> Unit)? = null,
)

/**
 * Parameters for the [ChatComponentFactory.ChannelMediaAttachmentsPreviewBottomBar] component.
 *
 * @param centerContent Composable lambda for center content in the bottom bar.
 * @param leadingContent Composable lambda for leading content in the bottom bar.
 * @param trailingContent Composable lambda for trailing content in the bottom bar.
 */
public data class ChannelMediaAttachmentsPreviewBottomBarParams(
    val centerContent: @Composable () -> Unit,
    val leadingContent: @Composable () -> Unit = {},
    val trailingContent: @Composable () -> Unit = {},
)

/**
 * Parameters for the [ChatComponentFactory.MessageFooterStatusIndicator] component.
 *
 * @param messageItem The message item state.
 * @param modifier Modifier for styling.
 */
public data class MessageFooterStatusIndicatorParams(
    val messageItem: MessageItemState,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerInputLeadingContent] component.
 *
 * @param state The message composer state.
 * @param onActiveCommandDismiss Handler when the active command is dismissed.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerInputLeadingContentParams(
    val state: MessageComposerState,
    val onActiveCommandDismiss: () -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachments] component.
 *
 * @param attachments The attachments currently selected in the composer.
 * @param onAttachmentRemoved Lambda invoked when an attachment is removed by the user.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAttachmentsParams(
    val attachments: List<Attachment>,
    val onAttachmentRemoved: (Attachment) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachmentAudioRecordItem] component.
 *
 * @param attachment The audio recording attachment to render.
 * @param playerState Current state of the audio player.
 * @param modifier Modifier for styling.
 * @param onPlayToggleClick Called when the play/pause button is tapped.
 * @param onThumbDragStart Called when the user starts dragging the waveform thumb.
 * @param onThumbDragStop Called when the user stops dragging, with the target seek fraction.
 * @param onAttachmentRemoved Called when the attachment is removed by the user.
 */
public data class MessageComposerAttachmentAudioRecordItemParams(
    val attachment: Attachment,
    val playerState: AudioPlayerState,
    val modifier: Modifier = Modifier,
    val onPlayToggleClick: (Attachment) -> Unit = {},
    val onThumbDragStart: (Attachment) -> Unit = {},
    val onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
    val onAttachmentRemoved: (Attachment) -> Unit = {},
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachmentMediaItem] component.
 *
 * @param attachment The image or video attachment to render.
 * @param onAttachmentRemoved Called when the attachment is removed by the user.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAttachmentMediaItemParams(
    val attachment: Attachment,
    val onAttachmentRemoved: (Attachment) -> Unit,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachmentMediaItemOverlay] component.
 *
 * @param attachmentType The MIME type of the attachment, e.g. [AttachmentType.VIDEO].
 */
public data class MessageComposerAttachmentMediaItemOverlayParams(
    val attachmentType: String?,
)

/**
 * Parameters for the [ChatComponentFactory.MessageComposerAttachmentFileItem] component.
 *
 * @param attachment The file attachment to render.
 * @param onAttachmentRemoved Called when the attachment is removed by the user.
 * @param modifier Modifier for styling.
 */
public data class MessageComposerAttachmentFileItemParams(
    val attachment: Attachment,
    val onAttachmentRemoved: (Attachment) -> Unit,
    val modifier: Modifier = Modifier,
)
