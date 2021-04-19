package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import java.io.File
import io.getstream.chat.android.offline.usecase.SendMessage as OfflineSendMessage

public interface SendMessage {
    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public operator fun invoke(
        message: Message,
    ): Call<Message> = invoke(message, null)

    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public operator fun invoke(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)?,
    ): Call<Message>
}

internal class SendMessageImpl(private val offlineSendMessage: OfflineSendMessage) : SendMessage {
    override operator fun invoke(
        message: Message,
        attachmentTransformer: ((at: Attachment, file: File) -> Attachment)?,
    ): Call<Message> = offlineSendMessage.invoke(message, attachmentTransformer)
}
