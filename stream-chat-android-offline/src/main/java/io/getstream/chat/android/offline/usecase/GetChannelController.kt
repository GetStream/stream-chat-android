package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.utils.validateCid
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController

public class GetChannelController internal constructor(private val domainImpl: ChatDomainImpl) {
    /**
     * Returns a ChannelController for given cid
     *
     * @param cid the full channel id. ie messaging:123
     *
     * @see io.getstream.chat.android.livedata.controller.ChannelController
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<ChannelController> {
        validateCid(cid)

        val channelControllerImpl = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            Result(channelControllerImpl)
        }
    }
}
