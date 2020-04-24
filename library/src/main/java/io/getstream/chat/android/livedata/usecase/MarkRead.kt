package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl
import java.security.InvalidParameterException

class MarkRead(var domainImpl: ChatDomainImpl) {
    operator fun invoke(cid: String): Call2<Boolean> {
        if (cid.isEmpty()) {
            throw InvalidParameterException("cid cant be empty")
        }

        val channelRepo = domainImpl.channel(cid)

        var runnable = suspend {

            channelRepo.markRead()
        }
        return CallImpl2<Boolean>(runnable, channelRepo.scope)
    }
}
