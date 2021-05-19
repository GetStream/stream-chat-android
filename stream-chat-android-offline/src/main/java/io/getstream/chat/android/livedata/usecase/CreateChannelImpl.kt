package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.ChatDomain

public sealed interface CreateChannel {
    /**
     * Creates a new channel. Will retry according to the retry policy if it fails
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     *
     * @param channel the channel object
     */
    @CheckResult
    public operator fun invoke(channel: Channel): Call<Channel>
}

internal class CreateChannelImpl(private val chatDomain: ChatDomain) : CreateChannel {
    override operator fun invoke(channel: Channel): Call<Channel> = chatDomain.createChannel(channel)
}
