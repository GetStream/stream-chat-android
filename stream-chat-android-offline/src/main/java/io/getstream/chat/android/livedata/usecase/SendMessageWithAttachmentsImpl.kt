@file:Suppress("DEPRECATION_ERROR")
package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import java.io.File
import io.getstream.chat.android.offline.usecase.SendMessageWithAttachments as OfflineSendMessageWithAttachments

@Deprecated(
    message = "Use sendMessage() and attachment.upload instead of this useCase",
    level = DeprecationLevel.ERROR,
)
public interface SendMessageWithAttachments {

    @Deprecated(
        message = "Use sendMessage() and attachment.upload instead of this useCase",
        level = DeprecationLevel.ERROR,
    )
    @CheckResult
    public operator fun invoke(
        cid: String,
        message: Message,
        files: List<File>,
        attachmentTransformer: Attachment.(file: File) -> Unit = { },
    ): Call<Message>
}

@Suppress("DEPRECATION_ERROR")
internal class SendMessageWithAttachmentsImpl(
    private val offlineSendMessageWithAttachments: OfflineSendMessageWithAttachments,
) : SendMessageWithAttachments {
    override fun invoke(
        cid: String,
        message: Message,
        files: List<File>,
        attachmentTransformer: Attachment.(file: File) -> Unit,
    ): Call<Message> = offlineSendMessageWithAttachments.invoke(cid, message, files, attachmentTransformer)
}
