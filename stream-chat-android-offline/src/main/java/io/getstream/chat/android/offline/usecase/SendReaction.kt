package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class SendReaction(private val domainImpl: ChatDomainImpl) {
    /**
     * Sends the reaction. Immediately adds the reaction to local storage and updates the reaction fields on the related message.
     * API call to send the reaction is retried according to the retry policy specified on the chatDomain
     * @param cid: the full channel id i. e. messaging:123
     * @param reaction the reaction to add
     * @param enforceUnique if set to true, new reaction will replace all reactions the user has on this message
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    operator fun invoke(cid: String, reaction: Reaction, enforceUnique: Boolean = false): Call<Reaction> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.sendReaction(reaction, enforceUnique)
        }
    }
}
