package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.validateCid

public interface ShowChannel {
    /**
     * Shows a channel that was previously hidden
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<Unit>
}

internal class ShowChannelImpl(private val domainImpl: ChatDomainImpl) : ShowChannel {
    override operator fun invoke(cid: String): Call<Unit> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.show()
        }
    }
}
