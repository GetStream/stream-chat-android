/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.uiutils.extension.containsLinks

internal class MessageValidator(
    private val appSettings: AppSettings,
    private val maxAttachmentCount: Int,
    var canSendLinks: Boolean = true,
    var maxMessageLength: Int = DEFAULT_MESSAGE_LENGTH,
) {

    fun validateMessage(message: String, attachments: List<Attachment>): List<ValidationError> = listOfNotNull(
        validateMessageLength(message),
        validateAttachmentCount(attachments),
        validateImageAttachmentSize(attachments),
        validateFileAttachmentSize(attachments),
        validateCanSendLinks(message),
    )

    private fun validateMessageLength(message: String): ValidationError? = message.length
        .takeIf { it > maxMessageLength }
        ?.let { ValidationError.MessageLengthExceeded(it, maxMessageLength) }

    private fun validateAttachmentCount(attachments: List<Attachment>): ValidationError? = attachments.count()
        .takeIf { it > maxAttachmentCount }
        ?.let { ValidationError.AttachmentCountExceeded(it, maxAttachmentCount) }

    private fun validateImageAttachmentSize(attachments: List<Attachment>): ValidationError? = attachments
        .filter { it.isImage() }
        .filter { it.fileSize > appSettings.app.imageUploadConfig.sizeLimitInBytes }
        .takeUnless { it.isEmpty() }
        ?.let { ValidationError.AttachmentSizeExceeded(it, appSettings.app.imageUploadConfig.sizeLimitInBytes) }

    private fun validateFileAttachmentSize(attachments: List<Attachment>): ValidationError? = attachments
        .filter { !it.isImage() }
        .filter { it.fileSize > appSettings.app.fileUploadConfig.sizeLimitInBytes }
        .takeUnless { it.isEmpty() }
        ?.let { ValidationError.AttachmentSizeExceeded(it, appSettings.app.fileUploadConfig.sizeLimitInBytes) }

    private fun validateCanSendLinks(message: String): ValidationError? = ValidationError.ContainsLinksWhenNotAllowed.takeIf { !canSendLinks && message.containsLinks() }

    internal companion object {
        /**
         * The default allowed number of characters in a message.
         */
        private const val DEFAULT_MESSAGE_LENGTH = 5000
    }
}
