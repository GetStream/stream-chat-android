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

package io.getstream.chat.android.client.attachment

import android.webkit.MimeTypeMap
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.EXTRA_UPLOAD_ID
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.UploadedFile
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import java.io.File

@InternalStreamChatApi
public class AttachmentUploader(private val client: ChatClient = ChatClient.instance()) {

    private val logger by taggedLogger("Chat:Uploader")

    /**
     * Uploads the given attachment.
     *
     * @param channelType The type of the channel.
     * @param channelId The ID of the channel.
     * @param attachment The attachment to be uploaded.
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
        progressCallback: ProgressCallback? = null,
    ): Result<Attachment> {
        val file = checkNotNull(attachment.upload) { "An attachment needs to have a non null attachment.upload value" }

        val mimeType: String = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            ?: attachment.mimeType ?: ""
        val attachmentType = mimeType.toAttachmentType()

        return if (attachmentType == AttachmentType.IMAGE) {
            logger.d { "[uploadAttachment] #uploader; uploading ${attachment.uploadId} as image" }
            uploadImage(
                channelType = channelType,
                channelId = channelId,
                file = file,
                progressCallback = progressCallback,
                attachment = attachment,
                mimeType = mimeType,
                attachmentType = attachmentType,
            )
        } else {
            logger.d { "[uploadAttachment] #uploader; uploading ${attachment.uploadId} as file" }
            uploadFile(
                channelType = channelType,
                channelId = channelId,
                file = file,
                progressCallback = progressCallback,
                attachment = attachment,
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
     * @param file The file that will be uploaded.
     * @param attachment The attachment to be uploaded.
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
        file: File,
        progressCallback: ProgressCallback?,
        attachment: Attachment,
        mimeType: String,
        attachmentType: AttachmentType,
    ): Result<Attachment> {
        logger.d {
            "[uploadImage] #uploader; mimeType: $mimeType, attachmentType: $attachmentType, " +
                "file: $file, cid: $channelType:$$channelId, attachment: $attachment"
        }
        val result = client.sendImage(channelType, channelId, file, progressCallback)
            .await()
        logger.v { "[uploadImage] #uploader; result: $result" }
        return when (result) {
            is Result.Success -> {
                val augmentedAttachment = attachment.augmentAttachmentOnSuccess(
                    file = file,
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
     * @param file The file that will be uploaded.
     * @param attachment The attachment to be uploaded.
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
        file: File,
        progressCallback: ProgressCallback?,
        attachment: Attachment,
        mimeType: String,
        attachmentType: AttachmentType,
    ): Result<Attachment> {
        logger.d {
            "[uploadFile] #uploader; mimeType: $mimeType, attachmentType: $attachmentType, " +
                "file: $file, cid: $channelType:$$channelId, attachment: $attachment"
        }
        val result = client.sendFile(channelType, channelId, file, progressCallback)
            .await()
        logger.v { "[uploadFile] #uploader; result: $result" }
        return when (result) {
            is Result.Success -> {
                val augmentedAttachment = attachment.augmentAttachmentOnSuccess(
                    file = file,
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
     * @param mimeType MimeType of uploaded attachment.
     * @param attachmentType File, video or picture enum instance.
     * @param uploadedFile Uploaded file data obtained from BE.
     * Usually returned for uploaded videos, can be null otherwise.
     */
    private fun Attachment.augmentAttachmentOnSuccess(
        file: File,
        mimeType: String,
        attachmentType: AttachmentType,
        uploadedFile: UploadedFile,
    ): Attachment = copy(
        name = file.name,
        fileSize = file.length().toInt(),
        mimeType = mimeType,
        uploadState = Attachment.UploadState.Success,
        title = title.takeUnless { it.isNullOrBlank() } ?: file.name,
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

    private fun String?.toAttachmentType(): AttachmentType {
        if (this == null) {
            return AttachmentType.FILE
        }
        return when {
            StreamCdnImageMimeTypes.isImageMimeTypeSupported(this) -> AttachmentType.IMAGE
            this.contains("video") -> AttachmentType.VIDEO
            else -> AttachmentType.FILE
        }
    }

    private enum class AttachmentType(private val value: String) {
        IMAGE("image"),
        VIDEO("video"),
        FILE("file"),
        ;

        override fun toString(): String = value
    }
}
