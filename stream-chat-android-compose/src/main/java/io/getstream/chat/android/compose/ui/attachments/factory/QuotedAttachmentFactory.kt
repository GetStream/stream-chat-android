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

package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isFile
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.AudioRecordAttachmentQuotedContent
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentQuotedContent
import io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentQuotedContent
import io.getstream.chat.android.ui.common.utils.extensions.hasLink

/**
 * An [AttachmentFactory] that validates attachments as files and uses [MediaAttachmentQuotedContent] in case the
 * attachment is a media attachment or [FileAttachmentQuotedContent] in case the attachment is a file to build the UI
 * for the quoted message.
 */
public object QuotedAttachmentFactory : AttachmentFactory(
    type = Type.BuiltIn.QUOTED,
    canHandle = {
        it.firstOrNull()
            ?.let { attachment ->
                attachment.isFile() ||
                    attachment.isAudioRecording() ||
                    attachment.isImage() ||
                    attachment.isVideo() ||
                    attachment.isGiphy() ||
                    attachment.hasLink()
            } ?: false
    },
    content = @Composable { modifier, attachmentState ->
        val attachment = attachmentState.message.attachments.first()

        when {
            attachment.isAudioRecording() -> AudioRecordAttachmentQuotedContent(
                modifier = modifier,
                attachment = attachment,
            )
            attachment.isImage() || attachment.isVideo() || attachment.isGiphy() || attachment.hasLink() -> {
                MediaAttachmentQuotedContent(modifier = modifier, attachment = attachment)
            }

            attachment.isFile() -> FileAttachmentQuotedContent(modifier = modifier, attachment = attachment)
        }
    },
)
