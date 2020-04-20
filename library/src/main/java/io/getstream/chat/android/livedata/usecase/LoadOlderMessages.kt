package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomain

class LoadOlderMessages(var domain: ChatDomain) {
    operator fun invoke (cid: String, messageLimit: Int): Call2<Channel> {
        val channelRepo = domain.channel(cid)
        var runnable = suspend {
            channelRepo.loadOlderMessages(messageLimit)
        }
        return CallImpl2<Channel>(runnable, channelRepo.scope)
    }
}