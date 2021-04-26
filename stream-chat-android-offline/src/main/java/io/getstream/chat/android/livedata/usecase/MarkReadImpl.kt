package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.livedata.ChatDomain

public interface MarkRead {
    /**
     * Marks the messages on the specified channel as read
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @return True if the mark read event was sent. False if there was no need to mark read
     *         (i. e. the messages are already marked as read).
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<Boolean>
}

internal class MarkReadImpl(private val chatDomain: ChatDomain) : MarkRead {
    override operator fun invoke(cid: String): Call<Boolean> = chatDomain.markRead(cid)
}
