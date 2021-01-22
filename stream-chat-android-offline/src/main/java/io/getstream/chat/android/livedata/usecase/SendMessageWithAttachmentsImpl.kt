package io.getstream.chat.android.livedata.usecase

import android.webkit.MimeTypeMap
import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.utils.validateCid
import java.io.File

public interface SendMessageWithAttachments {

    @Deprecated(
        message = "Use sendMessage() and attachment.upload instead of this useCase",
        level = DeprecationLevel.WARNING
    )
    @CheckResult
    public operator fun invoke(
        cid: String,
        message: Message,
        files: List<File>,
        attachmentTransformer: Attachment.(file: File) -> Unit = { },
    ): Call<Message>
}

internal class SendMessageWithAttachmentsImpl(private val domainImpl: ChatDomainImpl) :
    SendMessageWithAttachments {
    override fun invoke(
        cid: String,
        message: Message,
        files: List<File>,
        attachmentTransformer: Attachment.(file: File) -> Unit,
    ): Call<Message> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        message.enrichWithCid(cid)
        return CoroutineCall(domainImpl.scope) {
            val attachments = uploadFiles(channelController, files, attachmentTransformer)
            if (attachments.isError) {
                Result(attachments.error())
            } else {
                message.attachments.addAll(attachments.data())
                channelController.sendMessage(message)
            }
        }
    }

    private suspend fun uploadFiles(
        channelControllerImpl: ChannelControllerImpl,
        files: List<File>,
        attachmentTransformer: Attachment.(file: File) -> Unit,
    ): Result<List<Attachment>> =
        files.fold(Result(emptyList())) { acc, file ->
            if (acc.isError) {
                acc
            } else {
                val attachment = uploadFile(channelControllerImpl, file)
                if (attachment.isError) {
                    Result(attachment.error())
                } else {
                    Result(acc.data() + attachment.data().apply { attachmentTransformer(file) })
                }
            }
        }

    private suspend fun uploadFile(
        channelControllerImpl: ChannelControllerImpl,
        file: File,
    ): Result<Attachment> =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension).let { mimetype ->
            val pathResult = when (mimetype.isImageMimetype()) {
                true -> channelControllerImpl.sendImage(file)
                false -> channelControllerImpl.sendFile(file)
            }
            if (pathResult.isError) {
                Result(pathResult.error())
            } else {
                val path = pathResult.data()
                Result(
                    Attachment(
                        name = file.name,
                        fileSize = file.length().toInt(),
                        mimeType = mimetype,
                        url = path
                    ).apply {
                        when (mimetype.isImageMimetype()) {
                            true -> {
                                imageUrl = path
                                type = "image"
                            }
                            false -> {
                                assetUrl = path
                                type = "file"
                            }
                        }
                    }
                )
            }
        }

    private fun String?.isImageMimetype() = this?.contains("image") ?: false
}
