package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController

public sealed interface GetChannelController {
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

internal class GetChannelControllerImpl(private val chatDomain: ChatDomain) : GetChannelController {
    override operator fun invoke(cid: String): Call<ChannelController> =
        chatDomain.getChannelController(cid)
}
