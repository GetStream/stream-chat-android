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

package io.getstream.chat.android.ui.common.helper.internal

import androidx.core.content.MimeTypeFilter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.log.taggedLogger

/**
 * A filter that is used to filter out attachments that will not be accepted by the backend.
 *
 * Clients are able to modify the upload configuration in the dashboard and specify what
 * types of files and images they want to allow or block from being uploaded.
 *
 * @param chatClient An instance of the low level chat client to fetch upload config.
 */
public class AttachmentFilter(
    private val chatClient: ChatClient = ChatClient.instance(),
) {
    private val logger by taggedLogger("AttachmentFilter")

    /**
     * Filters out attachments that can be uploaded to the backend according to files
     * and images upload configurations.
     *
     * @param attachments A list of attachments.
     * @return A list of attachments allowed by the server.
     */
    public fun filterAttachments(attachments: List<AttachmentMetaData>): List<AttachmentMetaData> {
        val fileUploadConfig = chatClient.getAppSettings().app.fileUploadConfig
        val allowedFileExtensions = fileUploadConfig.allowedFileExtensions
        val allowedFileMimeTypes = fileUploadConfig.allowedMimeTypes.toTypedArray()
        val blockedFileExtensions = fileUploadConfig.blockedFileExtensions
        val blockedFileMimeTypes = fileUploadConfig.blockedMimeTypes.toTypedArray()

        val imageUploadConfig = chatClient.getAppSettings().app.imageUploadConfig
        val allowedImageExtensions = imageUploadConfig.allowedFileExtensions
        val allowedImageMimeTypes = imageUploadConfig.allowedMimeTypes.toTypedArray()
        val blockedImageExtensions = imageUploadConfig.blockedFileExtensions
        val blockedImageMimeTypes = imageUploadConfig.blockedMimeTypes.toTypedArray()

        return attachments.filter { attachment ->
            val isImage = attachment.type == AttachmentType.IMAGE

            matchesUploadConfig(
                attachment = attachment,
                allowedFileExtensions = if (isImage) allowedImageExtensions else allowedFileExtensions,
                allowedMimeTypes = if (isImage) allowedImageMimeTypes else allowedFileMimeTypes,
                blockedFileExtensions = if (isImage) blockedImageExtensions else blockedFileExtensions,
                blockedMimeTypes = if (isImage) blockedImageMimeTypes else blockedFileMimeTypes,
            )
        }
    }

    /**
     * Returns the list of supported MIME types by the server according to the upload config values.
     */
    public fun getSupportedMimeTypes(): List<String> {
        val default = listOf("*/*") // All files
        val fileUploadConfig = chatClient.getAppSettings().app.fileUploadConfig
        val imageUploadConfig = chatClient.getAppSettings().app.imageUploadConfig

        // Allowed
        val allowedFileMimeTypes = fileUploadConfig.allowedMimeTypes.toTypedArray()
        val allowedImageMimeTypes = imageUploadConfig.allowedMimeTypes.toTypedArray()
        // Blocked
        val blockedFileMimeTypes = fileUploadConfig.blockedMimeTypes.toTypedArray()
        val blockedImageMimeTypes = imageUploadConfig.blockedMimeTypes.toTypedArray()

        // Combined
        val allowed = allowedFileMimeTypes + allowedImageMimeTypes
        val blocked = blockedFileMimeTypes + blockedImageMimeTypes
        val result = allowed.filterNot { it in blocked }
        logger.d { "Supported MIME types: $result" }
        return result.ifEmpty { default }
    }

    /**
     * Checks if the attachment is allowed to be uploaded to the server according
     * to the upload config values.
     *
     * @param attachment The attachment to check.
     * @param allowedFileExtensions Allowed file extensions.
     * @param allowedFileExtensions Allowed mime types.
     * @param blockedFileExtensions Blocked mime types.
     * @param blockedMimeTypes Blocked mime types.
     * @return True if the attachment can be uploaded to the server.
     */
    private fun matchesUploadConfig(
        attachment: AttachmentMetaData,
        allowedFileExtensions: List<String>,
        allowedMimeTypes: Array<String>,
        blockedFileExtensions: List<String>,
        blockedMimeTypes: Array<String>,
    ): Boolean {
        /**
         * Blocked and allowed lists are mutually exclusive. It should not be possible to
         * configure both lists simultaneously in the dashboard.
         */
        val isWhiteList = allowedFileExtensions.isNotEmpty() || allowedMimeTypes.isNotEmpty()
        val isBlackList = blockedFileExtensions.isNotEmpty() || blockedMimeTypes.isNotEmpty()

        return when {
            isWhiteList -> matchesFileExtensionOrMimeType(attachment, allowedFileExtensions, allowedMimeTypes)
            isBlackList -> !matchesFileExtensionOrMimeType(attachment, blockedFileExtensions, blockedMimeTypes)
            else -> true
        }
    }

    /**
     * Checks if the attachment's extension or mime type matches against the provided lists.
     *
     * @param attachment The attachment to check.
     * @param fileExtensions The list of file extensions.
     * @param mimeTypes The array of mime types.
     * @return True if the attachment's extension or mime type matches against the provided lists.
     */
    private fun matchesFileExtensionOrMimeType(
        attachment: AttachmentMetaData,
        fileExtensions: List<String>,
        mimeTypes: Array<String>,
    ): Boolean {
        return matchesFileExtension(attachment, fileExtensions) ||
            matchesMimeType(attachment, mimeTypes)
    }

    /**
     * Checks if the attachment has a MIME type that matches any MIME type from
     * the provided list.
     *
     * @param attachment The attachment to check.
     * @param mimeTypes The list of allowed MIME types.
     * @return True if the attachment's mime type matches against any MIME type from the provided list.
     */
    private fun matchesMimeType(attachment: AttachmentMetaData, mimeTypes: Array<String>): Boolean {
        return try {
            MimeTypeFilter.matches(attachment.mimeType, mimeTypes) != null
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    /**
     * Checks if the attachment has an extension that matches any extension from
     * the provided list.
     *
     * @param attachment The attachment to check.
     * @param fileExtensions The list of allowed file extensions.
     * @return True if the attachment's extension matches against any extension from the provided list.
     */
    private fun matchesFileExtension(attachment: AttachmentMetaData, fileExtensions: List<String>): Boolean {
        return fileExtensions.any { extension ->
            attachment.title?.endsWith(extension, ignoreCase = true) ?: false
        }
    }
}
