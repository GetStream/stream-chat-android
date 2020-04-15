package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChannelController
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.client.utils.Result


class WatchChannel(var domain: ChatDomain) {
    operator fun invoke (cid: String, messageLimit: Int): Call2<ChannelController> {
        val channelRepo = domain.channel(cid)
        channelRepo.watch(messageLimit)
        var runnable = suspend {


            Result(channelRepo, null)
        }
        return CallImpl2<ChannelController>(runnable, channelRepo.scope)
    }
}