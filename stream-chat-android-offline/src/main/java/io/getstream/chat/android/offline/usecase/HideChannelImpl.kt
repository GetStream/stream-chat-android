package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.livedata.utils.validateCid
import io.getstream.chat.android.offline.ChatDomainImpl as NewChatDomainImpl

public interface HideChannel {
    /**
     * Hides the channel with the specified id
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param keepHistory: boolean, if you want to keep the history of this channel or not
     *
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    @CheckResult
    public operator fun invoke(cid: String, keepHistory: Boolean): Call<Unit>
}

internal class HideChannelImpl(private val domainImpl: NewChatDomainImpl) : HideChannel {
    override operator fun invoke(cid: String, keepHistory: Boolean): Call<Unit> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            val clearHistory = !keepHistory
            channelController.hide(clearHistory)
        }
    }
}
