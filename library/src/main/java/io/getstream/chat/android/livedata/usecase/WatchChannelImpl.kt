package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import kotlinx.coroutines.launch

interface WatchChannel {
    operator fun invoke(cid: String, messageLimit: Int): Call2<ChannelController>
}

class WatchChannelImpl(var domainImpl: ChatDomainImpl) : WatchChannel {
    override operator fun invoke(cid: String, messageLimit: Int): Call2<ChannelController> {
        val channelControllerImpl = domainImpl.channel(cid)
        val channelControllerI: ChannelController = channelControllerImpl

        if (messageLimit> 0) {
            channelControllerImpl.scope.launch {
                channelControllerImpl.watch(messageLimit)
            }
        }

        var runnable = suspend {
            Result(channelControllerI, null)
        }
        return CallImpl2<ChannelController>(
            runnable,
            channelControllerImpl.scope
        )
    }
}
