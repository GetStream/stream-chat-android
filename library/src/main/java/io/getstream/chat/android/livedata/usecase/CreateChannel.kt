package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomain

class CreateChannel(var domain: ChatDomain) {
    operator fun invoke (channel: Channel): Call2<Channel> {
        var runnable = suspend {
            domain.createChannel(channel)
        }
        return CallImpl2<Channel>(runnable, domain.scope)
    }
}