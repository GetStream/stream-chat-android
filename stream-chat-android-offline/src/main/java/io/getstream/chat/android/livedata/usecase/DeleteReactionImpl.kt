package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.livedata.ChatDomain

public interface DeleteReaction {
    /**
     * Deletes the specified reaction, request is retried according to the retry policy specified on the chatDomain
     * @param cid the full channel id, ie messaging:123
     * @param reaction the reaction to mark as deleted
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public operator fun invoke(cid: String, reaction: Reaction): Call<Message>
}

internal class DeleteReactionImpl(private val chatDomain: ChatDomain) : DeleteReaction {
    override operator fun invoke(cid: String, reaction: Reaction): Call<Message> =
        chatDomain.deleteReaction(cid, reaction)
}
