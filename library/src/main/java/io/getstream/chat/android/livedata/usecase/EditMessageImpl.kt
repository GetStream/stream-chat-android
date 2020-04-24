package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl
import java.security.InvalidParameterException

interface EditMessage {
    operator fun invoke(message: Message): Call2<Message>
}

class EditMessageImpl(var domainImpl: ChatDomainImpl) : EditMessage {
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
            channelRepo.editMessage(message)
        }
        return CallImpl2<Message>(runnable, channelRepo.scope)
    }
}
