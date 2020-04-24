package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl

interface CreateChannel {
    operator fun invoke(channel: Channel): Call2<Channel>
}

class CreateChannelImpl(var domainImpl: ChatDomainImpl) : CreateChannel {
    override operator fun invoke(channel: Channel): Call2<Channel> {
        var runnable = suspend {
            domainImpl.createChannel(channel)
        }
        return CallImpl2<Channel>(runnable, domainImpl.scope)
    }
}
