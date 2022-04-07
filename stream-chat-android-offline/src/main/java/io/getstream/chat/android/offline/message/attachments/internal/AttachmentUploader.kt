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

package io.getstream.chat.android.offline.message.attachments.internal

import android.webkit.MimeTypeMap
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import java.io.File

internal class AttachmentUploader(
    private val client: ChatClient = ChatClient.instance(),
) {

    internal suspend fun uploadAttachment(
        channelType: String,
        channelId: String,
        attachment: Attachment,
        progressCallback: ProgressCallback? = null,
    ): Result<Attachment> {
        val file = checkNotNull(attachment.upload) { "An attachment needs to have a non null attachment.upload value" }

        val mimeType: String? = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            ?: attachment.mimeType
        val attachmentType = mimeType.toAttachmentType()

        val result = if (attachmentType == AttachmentType.IMAGE) {
            val call = client.sendImage(channelType, channelId, file, progressCallback)
            call.await()
        } else {
            val call = client.sendFile(channelType, channelId, file, progressCallback)
            call.await()
        }
        return if (result.isSuccess) {
            val augmentedAttachment = attachment.augmentAttachmentOnSuccess(
                file = file,
                mimeType = mimeType ?: "",
                attachmentType = attachmentType,
                url = result.data()
            )
            augmentedAttachment.uploadState = Attachment.UploadState.Success
            progressCallback?.onSuccess(augmentedAttachment.url)
            Result(augmentedAttachment)
        } else {
            attachment.uploadState = Attachment.UploadState.Failed(result.error())
            progressCallback?.onError(result.error())
            Result(result.error())
        }
    }

    /**
     * Augment an attachment instance with data from uploaded file, mimeType, attachmentType and obtained from backend
     * url.
     *
     * @param file A file that has been uploaded.
     * @param mimeType MimeType of uploaded attachment.
     * @param attachmentType File, video or picture enum instance.
     * @param url URL obtained from BE.
     */
    private fun Attachment.augmentAttachmentOnSuccess(
        file: File,
        mimeType: String,
        attachmentType: AttachmentType,
        url: String,
    ): Attachment {
        return copy(
            name = file.name,
            fileSize = file.length().toInt(),
            mimeType = mimeType,
            url = url,
            uploadState = Attachment.UploadState.Success,
        ).apply {
            // If attachment type was not set, set it based on this value
            // determined by the MIME type guessed from the file's extension
            if (type == null) {
                type = attachmentType.toString()
            }
            if (attachmentType == AttachmentType.IMAGE) {
                imageUrl = url
            } else {
                assetUrl = url
            }
            if (title.isNullOrBlank()) {
                title = file.name
            }
        }
    }

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
        FILE("file");

        override fun toString(): String {
            return value
        }
    }
}
