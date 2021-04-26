package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController

public interface WatchChannel {
    /**
     * Watches the given channel and returns a ChannelController
     *
     * @param cid the full channel id. ie messaging:123
     * @param messageLimit how many messages to load on the first request
     *
     * @see io.getstream.chat.android.livedata.controller.ChannelController
     */
    @CheckResult
    public operator fun invoke(cid: String, messageLimit: Int): Call<ChannelController>
}

internal class WatchChannelImpl(private val chatDomain: ChatDomain) : WatchChannel {
    override operator fun invoke(cid: String, messageLimit: Int): Call<ChannelController> =
        chatDomain.watchChannel(cid, messageLimit)
}
