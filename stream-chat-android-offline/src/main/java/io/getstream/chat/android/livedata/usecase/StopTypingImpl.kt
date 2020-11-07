package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.validateCid

public interface StopTyping {
    /**
     * StopTyping should be called when the user submits the text and finishes typing
     *
     * @param cid: the full channel id IE messaging:123
     *
     * @return A call object with Boolean as the return type. True when a typing event was sent, false if it wasn't sent
     */
    public operator fun invoke(cid: String): Call<Boolean>
}

internal class StopTypingImpl(private val domainImpl: ChatDomainImpl) : StopTyping {
    override operator fun invoke(cid: String): Call<Boolean> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scopeIO) {
            channelController.stopTyping()
        }
    }
}
