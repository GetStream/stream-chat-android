package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.extensions.getCid
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

interface SendMessage {
    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @return A call object with Message as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    operator fun invoke(message: Message): Call2<Message>
}

class SendMessageImpl(var domainImpl: ChatDomainImpl) : SendMessage {
    override operator fun invoke(message: Message): Call2<Message> {
        val cid = message.getCid()
        validateCid(cid)

        val channelRepo = domainImpl.channel(cid)

        val runnable = suspend {
            channelRepo.sendMessage(message)
        }

        return CallImpl2(runnable, channelRepo.scope)
    }
}
