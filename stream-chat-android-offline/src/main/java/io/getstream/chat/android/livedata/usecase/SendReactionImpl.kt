package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.validateCid

public interface SendReaction {
    /**
     * Sends the reaction. Immediately adds the reaction to local storage and updates the reaction fields on the related message.
     * API call to send the reaction is retried according to the retry policy specified on the chatDomain
     * @param cid: the full channel id IE messaging:123
     * @param reaction the reaction to add
     * @return A call object with Reaction as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(cid: String, reaction: Reaction): Call<Reaction>
}

internal class SendReactionImpl(private val domainImpl: ChatDomainImpl) : SendReaction {
    override operator fun invoke(cid: String, reaction: Reaction): Call<Reaction> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scopeIO) {
            channelController.sendReaction(reaction)
        }
    }
}
