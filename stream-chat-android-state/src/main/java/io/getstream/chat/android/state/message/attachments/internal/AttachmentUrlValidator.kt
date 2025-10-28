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

package io.getstream.chat.android.state.message.attachments.internal

import io.getstream.chat.android.client.helpers.AttachmentHelper
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message

internal class AttachmentUrlValidator(private val attachmentHelper: AttachmentHelper = AttachmentHelper()) {

    internal fun updateValidAttachmentsUrl(
        newMessages: List<Message>,
        oldMessages: Map<String, Message>,
    ): List<Message> {
        if (oldMessages.isEmpty()) {
            return newMessages
        }
        return newMessages.map { newMessage -> updateValidAttachmentsUrl(newMessage, oldMessages[newMessage.id]) }
    }

    private fun updateValidAttachmentsUrl(newMessage: Message, oldMessage: Message?): Message = if (newMessage.attachments.isEmpty() || oldMessage == null) {
        newMessage
    } else {
        newMessage.copy(
            attachments = updateValidAttachmentsUrl(
                newMessage.attachments,
                oldMessage.attachments,
            ).toMutableList(),
        )
    }

    private fun updateValidAttachmentsUrl(
        newAttachments: List<Attachment>,
        oldAttachments: List<Attachment>,
    ): List<Attachment> = newAttachments.map { newAttachment ->
        updateValidAttachmentUrl(
            newAttachment,
            oldAttachments.firstOrNull { it.partialEquality(newAttachment) },
        )
    }

    private fun updateValidAttachmentUrl(newAttachment: Attachment, oldAttachment: Attachment?): Attachment = when {
        oldAttachment == null -> newAttachment
        oldAttachment.imageUrl.isNullOrEmpty() -> newAttachment
        oldAttachment.imageUrl == newAttachment.imageUrl -> newAttachment
        attachmentHelper.hasStreamImageUrl(oldAttachment).not() -> newAttachment
        attachmentHelper.hasValidImageUrl(oldAttachment).not() -> newAttachment
        else -> newAttachment.copy(imageUrl = oldAttachment.imageUrl)
    }

    private fun Attachment.partialEquality(other: Attachment): Boolean = authorName == other.authorName &&
        titleLink == other.titleLink &&
        mimeType == other.mimeType &&
        fileSize == other.fileSize &&
        title == other.title &&
        text == other.text &&
        type == other.type &&
        name == other.name &&
        fallback == other.fallback &&
        isDefault().not()

    private fun Attachment.isDefault(): Boolean = authorName.isNullOrBlank() &&
        titleLink.isNullOrBlank() &&
        fileSize == 0 &&
        title.isNullOrBlank() &&
        text.isNullOrBlank() &&
        name.isNullOrBlank() &&
        fallback.isNullOrBlank()
}
