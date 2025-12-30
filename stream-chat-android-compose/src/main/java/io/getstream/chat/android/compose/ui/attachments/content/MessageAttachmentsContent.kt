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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.hasLink

/**
 * Represents the content that's shown in message attachments. We decide if we need to show link previews or other
 * attachments.
 *
 * @param message The message that contains the attachments.
 * @param currentUser The current user that's logged in.
 * @param onLongItemClick Handler for long item taps on this content.
 * @param onMediaGalleryPreviewResult Handler when the user selects a message option in the
 * Media Gallery Preview screen.
 */
@Composable
public fun MessageAttachmentsContent(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    if (message.attachments.isNotEmpty()) {
        val (links, attachments) = message.attachments.partition { it.hasLink() && !it.isGiphy() }

        val linkFactory = if (links.isNotEmpty()) {
            ChatTheme.attachmentFactories.firstOrNull { it.canHandle(links) }
        } else {
            null
        }

        val attachmentFactory = if (attachments.isNotEmpty()) {
            ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }
        } else {
            null
        }

        val attachmentState = AttachmentState(
            message = message,
            isMine = message.user.id == currentUser?.id,
            onLongItemClick = onLongItemClick,
            onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
        )

        if (attachmentFactory != null) {
            attachmentFactory.content(Modifier, attachmentState)
        } else if (linkFactory != null) {
            linkFactory.content(Modifier.padding(8.dp), attachmentState)
        }
    }
}
