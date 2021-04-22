package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class DeleteReaction(private val domainImpl: ChatDomainImpl) {
    /**
     * Deletes the specified reaction, request is retried according to the retry policy specified on the chatDomain
     * @param cid the full channel id, ie messaging:123
     * @param reaction the reaction to mark as deleted
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    operator fun invoke(cid: String, reaction: Reaction): Call<Message> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.deleteReaction(reaction)
        }
    }
}
