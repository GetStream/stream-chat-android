package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.offline.usecase.ShowChannel as OfflineShowChannel

public interface ShowChannel {
    /**
     * Shows a channel that was previously hidden
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @see <a href="https://getstream.io/chat/docs/channel_delete/?language=kotlin">Hiding a channel</a>
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<Unit>
}

internal class ShowChannelImpl(private val offlineShowChannel: OfflineShowChannel) : ShowChannel {
    override operator fun invoke(cid: String): Call<Unit> = offlineShowChannel.invoke(cid)
}
