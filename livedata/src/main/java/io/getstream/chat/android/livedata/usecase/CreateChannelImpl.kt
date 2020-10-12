package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2

interface CreateChannel {
    /**
     * Creates a new channel. Will retry according to the retry policy if it fails
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     *
     * @param channel the channel object
     * @return A call object with Channel as the return type
     */
    operator fun invoke(channel: Channel): Call2<Channel>
}

class CreateChannelImpl(var domainImpl: ChatDomainImpl) : CreateChannel {
    override operator fun invoke(channel: Channel): Call2<Channel> {
        val runnable = suspend {
            domainImpl.createChannel(channel)
        }
        return CallImpl2(
            runnable,
            domainImpl.scope
        )
    }
}
