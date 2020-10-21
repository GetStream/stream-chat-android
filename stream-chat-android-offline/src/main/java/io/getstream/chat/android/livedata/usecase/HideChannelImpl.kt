package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface HideChannel {
    /**
     * Hides the channel with the specified id
     *
     * @param cid: the full channel id IE messaging:123
     * @param keepHistory: boolean, if you want to keep the history of this channel or not
     *
     * @return A call object with Unit as the return type
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    public operator fun invoke(cid: String, keepHistory: Boolean): Call2<Unit>
}

internal class HideChannelImpl(private val domainImpl: ChatDomainImpl) : HideChannel {
    override operator fun invoke(cid: String, keepHistory: Boolean): Call2<Unit> {
        validateCid(cid)
        val channelController = domainImpl.channel(cid)

        val runnable = suspend {
            val clearHistory = !keepHistory
            channelController.hide(clearHistory)
        }
        return CallImpl2(
            runnable,
            channelController.scope
        )
    }
}
