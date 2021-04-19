package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.usecase.LoadNewerMessages as OfflineLoadNewerMessages

public interface LoadNewerMessages {
    /**
     * Loads newer messages for the channel
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param messageLimit: how many new messages to load
     */
    @CheckResult
    public operator fun invoke(cid: String, messageLimit: Int): Call<Channel>
}

internal class LoadNewerMessagesImpl(private val offlineLoadNewerMessages: OfflineLoadNewerMessages) :
    LoadNewerMessages {
    override operator fun invoke(cid: String, messageLimit: Int): Call<Channel> =
        offlineLoadNewerMessages.invoke(cid, messageLimit)
}
