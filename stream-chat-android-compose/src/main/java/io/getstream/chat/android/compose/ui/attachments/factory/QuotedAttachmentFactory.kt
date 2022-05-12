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

package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentQuotedContent
import io.getstream.chat.android.compose.ui.attachments.content.ImageAttachmentQuotedContent
import io.getstream.chat.android.compose.ui.util.hasLink
import io.getstream.chat.android.compose.ui.util.isFile
import io.getstream.chat.android.compose.ui.util.isMedia

/**
 * An [AttachmentFactory] that validates attachments as files and uses [ImageAttachmentQuotedContent] in case the
 * attachment is a media attachment or [FileAttachmentQuotedContent] in case the attachment is a file to build the UI
 * for the quoted message.
 */
@Suppress("FunctionName")
public fun QuotedAttachmentFactory(): AttachmentFactory = AttachmentFactory(
    canHandle = {
        if (it.isEmpty()) return@AttachmentFactory false
        val attachment = it.first()
        attachment.isFile() || attachment.isMedia() || attachment.hasLink()
    },
    content = @Composable { modifier, attachmentState ->
        val attachment = attachmentState.message.attachments.first()

        val isFile = attachment.isFile()
        val isImage = attachment.isMedia()
        val isLink = attachment.hasLink()

        // TODO fix UI of attachment previews/icons
        when {
            isImage || isLink -> ImageAttachmentQuotedContent(modifier = modifier, attachment = attachment)
            isFile -> FileAttachmentQuotedContent(modifier = modifier, attachment = attachment)
        }
    }
)
