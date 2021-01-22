package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.utils.validateCid
import kotlinx.coroutines.launch

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

internal class WatchChannelImpl(private val domainImpl: ChatDomainImpl) : WatchChannel {
    override operator fun invoke(cid: String, messageLimit: Int): Call<ChannelController> {
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
