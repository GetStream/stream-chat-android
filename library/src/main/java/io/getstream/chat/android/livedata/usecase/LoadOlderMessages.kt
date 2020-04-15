package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomain

class LoadOlderMessages(var domain: ChatDomain) {
    operator fun invoke (cid: String, messageLimit: Int): Call2<Boolean> {
        val channelRepo = domain.channel(cid)
        var runnable = suspend {
            channelRepo.loadOlderMessages(messageLimit)
            Result(true, null)
        }
        return CallImpl2<Boolean>(runnable, channelRepo.scope)
    }
}