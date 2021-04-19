package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.usecase.ThreadLoadMore as OfflineThreadLoadMore

public interface ThreadLoadMore {
    /**
     * Loads more messages for the specified thread
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param parentId: the parentId of the thread
     * @param messageLimit: how many new messages to load
     */
    @CheckResult
    public operator fun invoke(cid: String, parentId: String, messageLimit: Int): Call<List<Message>>
}

internal class ThreadLoadMoreImpl(private val offlineThreadLoadMore: OfflineThreadLoadMore) : ThreadLoadMore {
    override operator fun invoke(cid: String, parentId: String, messageLimit: Int): Call<List<Message>> =
        offlineThreadLoadMore.invoke(cid, parentId, messageLimit)
}
