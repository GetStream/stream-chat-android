package io.getstream.chat.android.offline.channel.attachment

import android.webkit.MimeTypeMap
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.uploader.toProgressCallback
import io.getstream.chat.android.client.utils.Result
import java.io.File

internal class AttachmentUploader(
    private val client: ChatClient = ChatClient.instance(),
) {

    internal suspend fun uploadAttachment(
        channelType: String,
        channelId: String,
        attachment: Attachment,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null,
    ): Result<Attachment> {
        val file = checkNotNull(attachment.upload) { "An attachment needs to have a non null attachment.upload value" }

        val mimeType: String? = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension)
        val attachmentType = mimeType.toAttachmentType()

        val progressTracker = attachment.uploadId?.let {
            ProgressTrackerFactory.getOrCreate(it).apply {
                maxValue = file.length()
            }
        }
        val progressCallback = progressTracker?.toProgressCallback()

        val result = if (attachmentType == AttachmentType.IMAGE) {
            client.sendImage(channelType, channelId, file, progressCallback).await()
        } else {
            client.sendFile(channelType, channelId, file, progressCallback).await()
        }

        return if (result.isSuccess) {
            val augmentedAttachment = attachment.augmentAttachmentOnSuccess(
                file = file,
                mimeType = mimeType ?: "",
                attachmentType = attachmentType,
                url = result.data()
            ).let {
                // allow the user to change the format of the attachment
                if (attachmentTransformer != null) {
                    attachmentTransformer(it, file)
                } else {
                    it
                }
            }

            progressTracker?.setComplete(true)
            Result(augmentedAttachment)
        } else {
            progressTracker?.setComplete(false)
            Result(result.error())
        }
    }

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
            type = attachmentType.toString(),
        ).apply {
            if (attachmentType == AttachmentType.IMAGE) {
                imageUrl = url
            } else {
                assetUrl = url
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
