package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.utils.validateCid

public interface GetChannelController {
    /**
     * Returns a ChannelController for given cid
     *
     * @param cid the full channel id. ie messaging:123
     *
     * @see io.getstream.chat.android.livedata.controller.ChannelController
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<ChannelController>
}

internal class GetChannelControllerImpl(private val domainImpl: ChatDomainImpl) : GetChannelController {
    override operator fun invoke(cid: String): Call<ChannelController> {
        validateCid(cid)

        val channelControllerImpl = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            Result(channelControllerImpl)
        }
    }
}
