package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.offline.usecase.DeleteChannel as OfflineDeleteChannel

public interface DeleteChannel {
    /**
     * Deletes the channel with the specified id
     *
     * @param cid: the full channel id i. e. messaging:123
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<Unit>
}

internal class DeleteChannelImpl(private val offlineDeleteChannel: OfflineDeleteChannel) : DeleteChannel {
    override operator fun invoke(cid: String): Call<Unit> = offlineDeleteChannel.invoke(cid)
}
