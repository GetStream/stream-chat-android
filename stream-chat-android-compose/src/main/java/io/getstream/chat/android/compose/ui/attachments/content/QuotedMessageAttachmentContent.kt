/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the content that's shown in a quoted message attachments if the attachments are not empty.
 *
 * @param message The message that contains the attachments.
 * @param modifier Modifier for customization of attachment preview.
 * @param onLongItemClick Handler for long item taps on this content.
 * @param onImagePreviewResult Handler when the user selects a message option in the Image Preview screen.
 */
@Composable
public fun QuotedMessageAttachmentContent(
    message: Message,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit,
    onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
) {
    val attachments = message.attachments

    /**
     * Looks for quoted message attachment factory and if none can handle it looks for a standard attachment factory
     * that can handle the attachmentContent.
     */
    val quoteAttachmentFactory = if (attachments.isNotEmpty()) {
        val quotedFactory = ChatTheme.quoteAttachmentFactories.firstOrNull { it.canHandle(message.attachments.take(1)) }
        quotedFactory ?: ChatTheme.attachmentFactories.firstOrNull { it.canHandle(message.attachments.take(1)) }
    } else {
        null
    }

    val attachmentState = AttachmentState(
        message = message,
        onLongItemClick = onLongItemClick,
        onImagePreviewResult = onImagePreviewResult
    )

    quoteAttachmentFactory?.content?.invoke(
        modifier = modifier,
        attachmentState = attachmentState
    )
}
