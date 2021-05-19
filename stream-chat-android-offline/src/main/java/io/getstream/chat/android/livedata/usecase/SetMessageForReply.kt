package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain

public sealed interface SetMessageForReply {
    /**
     * Set the reply state for the channel.
     *
     * @param cid CID of the channel where reply state is being set.
     * @param message The message we want reply to. The null value means dismiss reply state.
     */
    @CheckResult
    public operator fun invoke(cid: String, message: Message?): Call<Unit>
}

internal class SetMessageForReplyImpl(private val chatDomain: ChatDomain) : SetMessageForReply {
    override fun invoke(cid: String, message: Message?): Call<Unit> = chatDomain.setMessageForReply(cid, message)
}
