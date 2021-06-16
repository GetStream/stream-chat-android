package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class CancelMessage(private val domainImpl: ChatDomainImpl) {
    /**
     * Cancels the message of "ephemeral" type. Removes the message from local storage.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    operator fun invoke(message: Message): Call<Boolean> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.cancelEphemeralMessage(message)
        }
    }
}
