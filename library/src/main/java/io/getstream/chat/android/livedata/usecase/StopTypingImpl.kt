package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import java.security.InvalidParameterException

interface StopTyping {
    operator fun invoke(cid: String): Call2<Boolean>
}

class StopTypingImpl(var domainImpl: ChatDomainImpl) : StopTyping {
    override operator fun invoke(cid: String): Call2<Boolean> {
        if (cid.isEmpty()) {
            throw InvalidParameterException("cid cant be empty")
        }

        val channelRepo = domainImpl.channel(cid)

        var runnable = suspend {

            channelRepo.stopTyping()
        }
        return CallImpl2<Boolean>(
            runnable,
            channelRepo.scope
        )
    }
}
