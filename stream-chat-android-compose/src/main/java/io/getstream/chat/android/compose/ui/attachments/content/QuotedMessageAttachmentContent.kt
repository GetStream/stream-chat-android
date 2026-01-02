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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * Represents the content that's shown in a quoted message if the attachments are not empty.
 *
 * @param message The message that contains the attachments.
 * @param currentUser The current user that's logged in.
 * @param onLongItemClick Handler for long item taps.
 * @param modifier Modifier for styling.
 * @param onMediaGalleryPreviewResult Handler when the user selects an option in the Media Gallery Preview screen.
 */
@Composable
public fun QuotedMessageAttachmentContent(
    message: Message,
    currentUser: User?,
    onLongItemClick: (Message) -> Unit,
    modifier: Modifier = Modifier,
    onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit = {},
) {
    val attachments = message.attachments

    /**
     * Looks for quoted message attachment factory and if none can handle it looks for a standard attachment factory
     * that can handle the attachmentContent.
     */
    val quoteAttachmentFactory = if (attachments.isNotEmpty()) {
        val quotedFactory = ChatTheme.quotedAttachmentFactories.firstOrNull {
            it.canHandle(message.attachments.take(1))
        }
        quotedFactory ?: ChatTheme.attachmentFactories.firstOrNull { it.canHandle(message.attachments.take(1)) }
    } else {
        null
    }

    val attachmentState = AttachmentState(
        message = message,
        isMine = message.user.id == currentUser?.id,
        onLongItemClick = onLongItemClick,
        onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
    )

    quoteAttachmentFactory?.content?.invoke(
        modifier,
        attachmentState,
    )
}
