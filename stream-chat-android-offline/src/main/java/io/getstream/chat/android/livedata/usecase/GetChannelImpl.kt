package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface GetChannel {
    /**
     * Returns a ChannelController for given cid
     *
     * @param cid the full channel id. ie messaging:123

     * @return A call object with ChannelController as the return type
     * @see io.getstream.chat.android.livedata.controller.ChannelController
     */
    public operator fun invoke(cid: String): Call2<ChannelController>
}

internal class GetChannelImpl(private val domainImpl: ChatDomainImpl) : GetChannel {
    override operator fun invoke(cid: String): Call2<ChannelController> {
        validateCid(cid)
        val channelControllerImpl = domainImpl.channel(cid)

        val runnable = suspend {
            Result(channelControllerImpl as ChannelController, null)
        }
        return CallImpl2(
            runnable,
            channelControllerImpl.scope
        )
    }
}
