package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class ShowChannel(private val domainImpl: ChatDomainImpl) {
    /**
     * Shows a channel that was previously hidden
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    @CheckResult
    operator fun invoke(cid: String): Call<Unit> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.show()
        }
    }
}
