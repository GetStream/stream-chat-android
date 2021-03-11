package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.utils.validateCid
import kotlinx.coroutines.launch
import io.getstream.chat.android.offline.ChannelController as NewChannelController
import io.getstream.chat.android.offline.ChatDomainImpl as NewChatDomainImpl

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
    public operator fun invoke(cid: String, messageLimit: Int): Call<NewChannelController>
}

internal class WatchChannelImpl(private val domainImpl: NewChatDomainImpl) : WatchChannel {
    override operator fun invoke(cid: String, messageLimit: Int): Call<NewChannelController> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)

        if (messageLimit > 0) {
            domainImpl.scope.launch {
                channelController.watch(messageLimit)
            }
        }

        return CoroutineCall(domainImpl.scope) {
            Result(channelController)
        }
    }
}
