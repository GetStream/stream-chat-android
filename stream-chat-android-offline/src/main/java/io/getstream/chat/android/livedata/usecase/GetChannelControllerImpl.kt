package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.offline.usecase.GetChannelController as OfflineGetChannelController

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

internal class GetChannelControllerImpl(private val offlineGetChannelController: OfflineGetChannelController) :
    GetChannelController {
    override operator fun invoke(cid: String): Call<ChannelController> =
        offlineGetChannelController.invoke(cid).map(::ChannelControllerImpl)
}
