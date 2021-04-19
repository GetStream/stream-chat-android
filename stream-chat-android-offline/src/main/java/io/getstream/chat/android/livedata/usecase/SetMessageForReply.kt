package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.usecase.SetMessageForReply as OfflineSetMessageForReply

public interface SetMessageForReply {
    /**
     * Set the reply state for the channel.
     *
     * @param cid CID of the channel where reply state is being set.
     * @param message The message we want reply to. The null value means dismiss reply state.
     */
    @CheckResult
    public operator fun invoke(cid: String, message: Message?): Call<Unit>
}

internal class SetMessageForReplyImpl(private val offlineSetMessageForReplyImpl: OfflineSetMessageForReply) :
    SetMessageForReply {
    override fun invoke(cid: String, message: Message?): Call<Unit> = offlineSetMessageForReplyImpl.invoke(cid, message)
}
