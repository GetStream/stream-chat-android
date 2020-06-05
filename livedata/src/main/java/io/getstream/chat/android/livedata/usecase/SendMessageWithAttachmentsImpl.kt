package io.getstream.chat.android.livedata.usecase

import android.webkit.MimeTypeMap
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid
import java.io.File

interface SendMessageWithAttachments {

    operator fun invoke(cid: String, messageText: String, files: List<File>): Call2<Message>
}

class SendMessageWithAttachmentsImpl(private val domainImpl: ChatDomainImpl) : SendMessageWithAttachments {
    override fun invoke(cid: String, messageText: String, files: List<File>): Call2<Message> {
        validateCid(cid)
        val channel = domainImpl.channel(cid)
        val runnable = suspend {
            val message = Message(cid = cid, text = messageText)
            val attachments = uploadFiles(channel, files)
            if (attachments.isError) {
                Result(attachments.error())
            } else {
                message.attachments = attachments.data().toMutableList()
                channel.sendMessage(message)
            }
        }
        return CallImpl2(
            runnable,
            channel.scope
        )
    }

    private suspend fun uploadFiles(channelControllerImpl: ChannelControllerImpl, files: List<File>): Result<List<Attachment>> =
        files.fold(Result<List<Attachment>>(listOf())) { acc, file ->
            if (acc.isError) {
                acc
            } else {
                val attachment = uploadFile(channelControllerImpl, file)
                if (attachment.isError) {
                    Result(attachment.error())
                } else {
                    Result(acc.data() + attachment.data())
                }
            }
        }

    private suspend fun uploadFile(channelControllerImpl: ChannelControllerImpl, file: File): Result<Attachment> =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension).let { mimetype ->
            val pathResult =  when (mimetype.isImageMimetype()) {
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
                            true -> { this.imageUrl = path }
                            false -> { this.assetUrl = path }
                        }
                    }
                )
            }
        }
}

private fun String?.isImageMimetype() = this?.contains("image") ?: false
