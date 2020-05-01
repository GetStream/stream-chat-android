package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import java.security.InvalidParameterException

interface SendMessage {
    operator fun invoke(message: Message): Call2<Message>
}

class SendMessageImpl(var domainImpl: ChatDomainImpl) : SendMessage {
    override operator fun invoke(message: Message): Call2<Message> {
        var cid = message.cid
        if (cid.isEmpty()) {
            cid = message.channel.cid
        }
        if (cid.isEmpty()) {
            throw InvalidParameterException("message.cid cant be empty")
        }

        val channelRepo = domainImpl.channel(cid)

        var runnable = suspend {
            channelRepo.sendMessage(message)
        }
        return CallImpl2<Message>(
            runnable,
            channelRepo.scope
        )
    }
}
