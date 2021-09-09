package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.ChatDomainImpl

internal class CreateChannel(private val domainImpl: ChatDomainImpl) {
    /**
     * Creates a new channel. Will retry according to the retry policy if it fails.
     *
     * @param channel The channel object.
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    operator fun invoke(channel: Channel): Call<Channel> {
        return CoroutineCall(domainImpl.scope) {
            domainImpl.createNewChannel(channel)
        }
    }
}
