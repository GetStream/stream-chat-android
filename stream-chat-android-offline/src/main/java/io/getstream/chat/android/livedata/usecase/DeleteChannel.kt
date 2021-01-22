package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.livedata.ChatDomainImpl

public interface DeleteChannel {
    /**
     * Deletes the channel with the specified id
     *
     * @param cid: the full channel id i. e. messaging:123
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<Unit>
}

internal class DeleteChannelImpl(private val domainImpl: ChatDomainImpl) : DeleteChannel {
    override operator fun invoke(cid: String): Call<Unit> {
        return CoroutineCall(domainImpl.scope) {
            domainImpl.channel(cid).delete()
        }
    }
}
