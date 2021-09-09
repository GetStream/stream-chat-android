package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class ShuffleGiphy(private val domainImpl: ChatDomainImpl) {
    /**
     * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message The message to send.
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    operator fun invoke(message: Message): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.shuffleGiphy(message)
        }
    }
}
