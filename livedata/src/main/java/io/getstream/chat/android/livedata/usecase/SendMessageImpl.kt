package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid
import java.io.File

interface SendMessage {
    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @return A call object with Message as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    operator fun invoke(message: Message, attachmentTransformer: ((at: Attachment, path: String, file: File) -> Attachment)? = null): Call2<Message>
}

class SendMessageImpl(var domainImpl: ChatDomainImpl) : SendMessage {
    override operator fun invoke(message: Message, attachmentTransformer: ((at: Attachment, path: String, file: File) -> Attachment)?): Call2<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelRepo = domainImpl.channel(cid)

        val runnable = suspend {
            channelRepo.sendMessage(message, attachmentTransformer)
        }

        return CallImpl2(runnable, channelRepo.scope)
    }
}
