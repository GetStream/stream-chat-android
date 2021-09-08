package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.offline.ChatDomainImpl

internal class DeleteChannel(private val domainImpl: ChatDomainImpl) {
    /**
     * Deletes the channel with the specified id.
     *
     * @param cid The full channel id i. e. messaging:123.
     */
    @CheckResult
    operator fun invoke(cid: String): Call<Unit> {
        return CoroutineCall(domainImpl.scope) {
            domainImpl.channel(cid).delete()
        }
    }
}
