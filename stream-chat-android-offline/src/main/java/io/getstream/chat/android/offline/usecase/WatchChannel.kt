package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.utils.validateCid
import kotlinx.coroutines.launch

internal class WatchChannel(private val domainImpl: ChatDomainImpl) {
    /**
     * Watches the given channel and returns a ChannelController
     *
     * @param cid the full channel id. ie messaging:123
     * @param messageLimit how many messages to load on the first request
     *
     * @see io.getstream.chat.android.offline.channel.ChannelController
     */
    @CheckResult
    operator fun invoke(cid: String, messageLimit: Int): Call<ChannelController> {
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
