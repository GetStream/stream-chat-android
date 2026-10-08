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

package io.getstream.chat.android.client.attachment

import android.webkit.MimeTypeMap
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.EXTRA_UPLOAD_ID
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.UploadedFile
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

@InternalStreamChatApi
public class AttachmentUploader(private val client: ChatClient = ChatClient.instance()) {

    private val logger by taggedLogger("Chat:Uploader")

    /**
     * Uploads the given attachment.
     *
     * @param channelType The type of the channel.
     * @param channelId The ID of the channel.
     * @param attachment The attachment to be uploaded.
     * @param messageId The id of the message the attachment belongs to, or null when the upload is not part
     * of sending a message.
     * @param progressCallback Used to listen to file upload
     * progress, success, and failure.
     *
     * @return The resulting uploaded attachment.
     */
    @InternalStreamChatApi
    public suspend fun uploadAttachment(
        channelType: String,
        channelId: String,
        attachment: Attachment,
        messageId: String? = null,
        progressCallback: ProgressCallback? = null,
    ): Result<Attachment> {
        val originalFile =
            checkNotNull(attachment.upload) { "An attachment needs to have a non null attachment.upload value" }
        val file = withContext(DispatcherProvider.IO) {
            runCatching { client.fileTransformer.transform(originalFile) }
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            val failure = Result.Failure(Error.ThrowableError("Failed to transform ${originalFile.name}", error))
            return onFailedUpload(attachment, failure, progressCallback)
        }

        // Prefer the transformed file's type, as the transformer may change the format. Fall back to the original
        // when the transformer writes to a file whose extension says nothing about the format (e.g. ".tmp").
        val transformedMimeType = file.mimeTypeFromExtension()
        val mimeType: String = transformedMimeType
            ?: originalFile.mimeTypeFromExtension()
            ?: attachment.mimeType ?: ""
        val attachmentType = mimeType.toAttachmentType()
        val formatChanged = !file.extension.equals(originalFile.extension, ignoreCase = true)
        val name = if (transformedMimeType != null && formatChanged) {
            "${originalFile.nameWithoutExtension}.${file.extension}"
        } else {
            originalFile.name
        }

        return if (attachmentType == AttachmentType.IMAGE) {
            logger.d { "[uploadAttachment] #uploader; uploading ${attachment.uploadId} as image" }
            uploadImage(
                channelType = channelType,
                channelId = channelId,
                messageId = messageId,
                file = file,
                progressCallback = progressCallback,
                attachment = attachment,
                name = name,
                mimeType = mimeType,
                attachmentType = attachmentType,
            )
        } else {
            logger.d { "[uploadAttachment] #uploader; uploading ${attachment.uploadId} as file" }
            uploadFile(
                channelType = channelType,
                channelId = channelId,
                messageId = messageId,
                file = file,
                progressCallback = progressCallback,
                attachment = attachment,
                name = name,
                mimeType = mimeType,
                attachmentType = attachmentType,
            )
        }
    }

    /**
     * Uploads an image attachment.
     *
     * @param channelType The type of the channel.
     * @param channelId The ID of the channel.
     * @param messageId The id of the message the attachment belongs to, or null when the upload is not part
     * of sending a message.
     * @param file The file that will be uploaded.
     * @param attachment The attachment to be uploaded.
     * @param name The name to give the uploaded attachment.
     * @param progressCallback Used to listen to file upload
     * progress, success, and failure.
     * @param mimeType The mime type of the attachment that will be uploaded,
     * e.g. image/jpeg.
     * @param attachmentType The type of the attachment, e.g. "video", "audio", etc.
     *
     * @return The resulting uploaded attachment.
     */
    @Suppress("LongParameterList")
    private suspend fun uploadImage(
        channelType: String,
        channelId: String,
        messageId: String?,
        file: File,
        progressCallback: ProgressCallback?,
        attachment: Attachment,
        name: String,
        mimeType: String,
        attachmentType: AttachmentType,
    ): Result<Attachment> {
        logger.d {
            "[uploadImage] #uploader; mimeType: $mimeType, attachmentType: $attachmentType, " +
                "file: $file, cid: $channelType:$$channelId, attachment: $attachment"
        }
        val result = client.api.sendImage(channelType, channelId, file, messageId, progressCallback, transform = false)
            .await()
        logger.v { "[uploadImage] #uploader; result: $result" }
        return when (result) {
            is Result.Success -> {
                val augmentedAttachment = attachment.augmentAttachmentOnSuccess(
                    file = file,
                    name = name,
                    mimeType = mimeType,
                    attachmentType = attachmentType,
                    uploadedFile = result.value,
                )

                onSuccessfulUpload(
                    augmentedAttachment = augmentedAttachment,
                    progressCallback = progressCallback,
                )
            }
            is Result.Failure -> {
                onFailedUpload(
                    attachment = attachment,
                    result = result,
                    progressCallback = progressCallback,
                )
            }
        }
    }

    /**
     * Uploads a file attachment.
     *
     * @param channelType The type of the channel.
     * @param channelId The ID of the channel.
     * @param messageId The id of the message the attachment belongs to, or null when the upload is not part
     * of sending a message.
     * @param file The file that will be uploaded.
     * @param attachment The attachment to be uploaded.
     * @param name The name to give the uploaded attachment.
     * @param progressCallback Used to listen to file upload
     * progress, success, and failure.
     * @param mimeType The mime type of the attachment that will be uploaded,
     * e.g. image/jpeg.
     * @param attachmentType The type of the attachment, e.g. "video", "audio", etc.
     *
     * @return The resulting uploaded attachment.
     */
    @Suppress("LongParameterList")
    private suspend fun uploadFile(
        channelType: String,
        channelId: String,
        messageId: String?,
        file: File,
        progressCallback: ProgressCallback?,
        attachment: Attachment,
        name: String,
        mimeType: String,
        attachmentType: AttachmentType,
    ): Result<Attachment> {
        logger.d {
            "[uploadFile] #uploader; mimeType: $mimeType, attachmentType: $attachmentType, " +
                "file: $file, cid: $channelType:$$channelId, attachment: $attachment"
        }
        val result = client.api.sendFile(channelType, channelId, file, messageId, progressCallback, transform = false)
            .await()
        logger.v { "[uploadFile] #uploader; result: $result" }
        return when (result) {
            is Result.Success -> {
                val augmentedAttachment = attachment.augmentAttachmentOnSuccess(
                    file = file,
                    name = name,
                    mimeType = mimeType,
                    attachmentType = attachmentType,
                    uploadedFile = result.value,
                )

                onSuccessfulUpload(
                    augmentedAttachment = augmentedAttachment,
                    progressCallback = progressCallback,
                )
            }
            is Result.Failure -> {
                onFailedUpload(
                    attachment = attachment,
                    result = result,
                    progressCallback = progressCallback,
                )
            }
        }
    }

    /**
     * Updates the upload state and calls the appropriate [ProgressCallback]
     * method.
     *
     * @param augmentedAttachment The attachment pre filled with
     * the appropriate fields after the file contained in the attachment
     * was uploaded.
     * @param progressCallback Used to listen to file upload
     * progress, success, and failure.
     *
     * @return The resulting successfully uploaded attachment.
     * */
    private fun onSuccessfulUpload(
        augmentedAttachment: Attachment,
        progressCallback: ProgressCallback?,
    ): Result<Attachment> {
        logger.d { "[onSuccessfulUpload] #uploader; attachment ${augmentedAttachment.uploadId} uploaded successfully" }
        progressCallback?.onSuccess(augmentedAttachment.assetUrl)
        return Result.Success(augmentedAttachment.copy(uploadState = Attachment.UploadState.Success))
    }

    /**
     * Updates the upload state and calls the appropriate [ProgressCallback]
     * method.
     *
     * @param attachment The attachment that has failed to upload.
     * @param result The result of the failed upload.
     * @param progressCallback Used to listen to file upload
     * progress, success, and failure.
     *
     * @return Returns a [Result] containing a [io.getstream.result.Error]
     * */
    private fun onFailedUpload(
        attachment: Attachment,
        result: Result.Failure,
        progressCallback: ProgressCallback?,
    ): Result<Attachment> {
        logger.e { "[onFailedUpload] #uploader; attachment ${attachment.uploadId} upload failed: ${result.value}" }
        progressCallback?.onError(result.value)
        return Result.Failure(result.value)
    }

    /**
     * Augment an attachment instance with data from uploaded file, mimeType, attachmentType and obtained from backend
     * url.
     *
     * @param file A file that has been uploaded.
     * @param name The name to give the uploaded attachment.
     * @param mimeType MimeType of uploaded attachment.
     * @param attachmentType File, video or picture enum instance.
     * @param uploadedFile Uploaded file data obtained from BE.
     * Usually returned for uploaded videos, can be null otherwise.
     */
    private fun Attachment.augmentAttachmentOnSuccess(
        file: File,
        name: String,
        mimeType: String,
        attachmentType: AttachmentType,
        uploadedFile: UploadedFile,
    ): Attachment {
        return copy(
            name = name,
            fileSize = file.length().toInt(),
            mimeType = mimeType,
            uploadState = Attachment.UploadState.Success,
            title = title.takeUnless { it.isNullOrBlank() } ?: name,
            thumbUrl = uploadedFile.thumbUrl,
            type = type ?: attachmentType.toString(),
            imageUrl = when (attachmentType) {
                AttachmentType.IMAGE -> uploadedFile.file
                AttachmentType.VIDEO -> uploadedFile.thumbUrl
                else -> imageUrl
            },
            assetUrl = uploadedFile.file,
            extraData = (extraData + uploadedFile.extraData) - EXTRA_UPLOAD_ID,
        )
    }

    private fun File.mimeTypeFromExtension(): String? =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

    private fun String?.toAttachmentType(): AttachmentType {
        if (this == null) {
            return AttachmentType.FILE
        }
        return when {
            StreamCdnImageMimeTypes.isImageMimeTypeSupported(this) -> AttachmentType.IMAGE
            this.contains("video") -> AttachmentType.VIDEO
            // A MIME type is type/subtype, so audio means the top-level type, not the word appearing anywhere.
            this.startsWith("audio/") -> AttachmentType.AUDIO
            else -> AttachmentType.FILE
        }
    }

    private enum class AttachmentType(private val value: String) {
        IMAGE("image"),
        VIDEO("video"),
        AUDIO("audio"),
        FILE("file"),
        ;

        override fun toString(): String {
            return value
        }
    }
}
