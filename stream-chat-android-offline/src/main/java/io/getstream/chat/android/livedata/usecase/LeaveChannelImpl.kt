package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.offline.usecase.LeaveChannel as OfflineLeaveChannel

public interface LeaveChannel {
    /**
     * Leaves the channel with the specified id
     *
     * @param cid: the full channel id i. e. messaging:123
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<Unit>
}

internal class LeaveChannelImpl(private val offlineLeaveChannel: OfflineLeaveChannel) : LeaveChannel {
    override operator fun invoke(cid: String): Call<Unit> = offlineLeaveChannel.invoke(cid)
}
