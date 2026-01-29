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

package io.getstream.chat.android.compose.state.messages.attachments

import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message

/**
 * Represents the state of Attachment items, used to render and add handlers required for the attachment to work.
 *
 * @param message Data that represents the message information.
 * @param filteredAttachments List of attachments of the relevant type, e.g.
 * [ChatComponentFactory.MediaAttachmentContent] will see only media attachments here. All attachments are still
 * available in [message] ([Message.attachments]).
 * @param isMine Flag that indicates if the message is from the current user.
 * @param onLongItemClick Handler for a long click on the message item.
 * @param onMediaGalleryPreviewResult Handler used when the user selects an action to perform from
 * [io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewActivity].
 */
public data class AttachmentState(
    val message: Message,
    val filteredAttachments: List<Attachment>,
    val isMine: Boolean = false,
    val onLongItemClick: (Message) -> Unit = {},
    val onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
)
