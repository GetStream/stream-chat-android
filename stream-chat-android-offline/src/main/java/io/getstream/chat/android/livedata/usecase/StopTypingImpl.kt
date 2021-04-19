package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.offline.usecase.StopTyping as OfflineStopTyping

public interface StopTyping {
    /**
     * StopTyping should be called when the user submits the text and finishes typing
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param parentId set this field to `message.id` to indicate that typing event is happening in a thread
     *
     * @return True when a typing event was sent, false if it wasn't sent.
     */
    @CheckResult
    public operator fun invoke(cid: String, parentId: String? = null): Call<Boolean>
}

internal class StopTypingImpl(private val offlineStopTyping: OfflineStopTyping) : StopTyping {
    override operator fun invoke(cid: String, parentId: String?): Call<Boolean> =
        offlineStopTyping.invoke(cid, parentId)
}
