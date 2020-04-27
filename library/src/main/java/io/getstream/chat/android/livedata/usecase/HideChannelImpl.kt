package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelController
import kotlinx.coroutines.launch
import java.security.InvalidParameterException

interface HideChannel {
    operator fun invoke(cid: String, keepHistory: Boolean): Call2<Unit>
}

class HideChannelImpl(var domainImpl: ChatDomainImpl) : HideChannel {
    override operator fun invoke(cid: String, clearHistory: Boolean): Call2<Unit> {
        if (cid.isEmpty()) {
            throw InvalidParameterException("cid cant be empty")
        }
        val channelController = domainImpl.channel(cid)

        var runnable = suspend {
            channelController.hide(clearHistory)
        }
        return CallImpl2<Unit>(runnable, channelController.scope)
    }
}
