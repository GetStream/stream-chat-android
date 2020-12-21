package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.validateCid
import java.io.File

public interface SendMessage {
    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(message: Message, attachmentTransformer: ((at: Attachment, file: File) -> Attachment)? = null): Call<Message>
}

internal class SendMessageImpl(private val domainImpl: ChatDomainImpl) : SendMessage {
    override operator fun invoke(message: Message, attachmentTransformer: ((at: Attachment, file: File) -> Attachment)?): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.sendMessage(message, attachmentTransformer)
        }
    }
}
