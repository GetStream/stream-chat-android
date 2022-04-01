package com.getstream.sdk.chat.utils

import androidx.core.content.MimeTypeFilter
import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.FileUploadConfig

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

    /**
     * Filters out file attachments that can't be uploaded to the backend according
     * to the file upload configuration in the dashboard.
     *
     * @param attachments A list of file attachments.
     * @return A list of file attachments allowed by the server.
     */
    public fun filterFileAttachments(attachments: List<AttachmentMetaData>): List<AttachmentMetaData> {
        return filterAttachments(attachments, chatClient.getAppSettings().app.fileUploadConfig)
    }

    /**
     * Filters out image attachments that can't be uploaded to the backend according
     * to the image upload configuration in the dashboard.
     *
     * @param attachments A list of image attachments.
     * @return A list of image attachments allowed by the server.
     */
    public fun filterImageAttachments(attachments: List<AttachmentMetaData>): List<AttachmentMetaData> {
        return filterAttachments(attachments, chatClient.getAppSettings().app.imageUploadConfig)
    }

    /**
     * Filters out attachments that can not be uploaded to the backend according
     * to the corresponding upload configuration.
     *
     * @param attachments A list of attachments.
     * @param fileUploadConfig The configuration of file upload.
     * @return A list of attachments allowed by the server.
     */
    private fun filterAttachments(
        attachments: List<AttachmentMetaData>,
        fileUploadConfig: FileUploadConfig,
    ): List<AttachmentMetaData> {
        val allowedFileExtensions = fileUploadConfig.allowedFileExtensions
        val allowedMimeTypes = fileUploadConfig.allowedMimeTypes.toTypedArray()
        val blockedFileExtensions = fileUploadConfig.blockedFileExtensions
        val blockedMimeTypes = fileUploadConfig.blockedMimeTypes.toTypedArray()

        return attachments.filter { attachment ->
            /**
             * Blocked and allowed lists are mutually exclusive. It should not be possible to
             * configure both lists simultaneously in the dashboard.
             */
            val isWhiteList = allowedFileExtensions.isNotEmpty() || allowedMimeTypes.isNotEmpty()
            val isBlackList = blockedFileExtensions.isNotEmpty() || blockedMimeTypes.isNotEmpty()

            when {
                isWhiteList -> matchesFileExtensionOrMimeType(attachment, allowedFileExtensions, allowedMimeTypes)
                isBlackList -> !matchesFileExtensionOrMimeType(attachment, blockedFileExtensions, blockedMimeTypes)
                else -> true
            }
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
        } catch (e: IllegalArgumentException) {
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
