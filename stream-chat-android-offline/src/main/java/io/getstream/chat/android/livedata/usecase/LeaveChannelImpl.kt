package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.livedata.ChatDomain

public sealed interface LeaveChannel {
    /**
     * Leaves the channel with the specified id
     *
     * @param cid: the full channel id i. e. messaging:123
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<Unit>
}

internal class LeaveChannelImpl(private val chatDomain: ChatDomain) : LeaveChannel {
    override operator fun invoke(cid: String): Call<Unit> = chatDomain.leaveChannel(cid)
}
