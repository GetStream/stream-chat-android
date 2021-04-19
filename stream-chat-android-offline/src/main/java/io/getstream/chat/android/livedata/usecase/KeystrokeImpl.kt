package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.offline.usecase.Keystroke as OfflineKeystroke

public interface Keystroke {
    /**
     * Keystroke should be called whenever a user enters text into the message input
     * It automatically calls stopTyping when the user stops typing after 5 seconds
     *
     * @param cid the full channel id i. e. messaging:123
     * @param parentId set this field to `message.id` to indicate that typing event is happening in a thread
     *
     * @return True when a typing event was sent, false if it wasn't sent
     */
    @CheckResult
    public operator fun invoke(cid: String, parentId: String? = null): Call<Boolean>
}

internal class KeystrokeImpl(private val offlineKeystroke: OfflineKeystroke) : Keystroke {
    override operator fun invoke(cid: String, parentId: String?): Call<Boolean> = offlineKeystroke.invoke(cid, parentId)
}
