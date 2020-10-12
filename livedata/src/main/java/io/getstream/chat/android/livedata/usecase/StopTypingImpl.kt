package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

interface StopTyping {
    /**
     * StopTyping should be called when the user submits the text and finishes typing
     *
     * @param cid: the full channel id IE messaging:123
     *
     * @return A call object with Boolean as the return type. True when a typing event was sent, false if it wasn't sent
     */
    operator fun invoke(cid: String): Call2<Boolean>
}

class StopTypingImpl(var domainImpl: ChatDomainImpl) : StopTyping {
    override operator fun invoke(cid: String): Call2<Boolean> {
        validateCid(cid)

        val channelRepo = domainImpl.channel(cid)

        val runnable = suspend {

            channelRepo.stopTyping()
        }
        return CallImpl2(
            runnable,
            channelRepo.scope
        )
    }
}
