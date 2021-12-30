package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.extensions.populateMentions
import io.getstream.chat.android.offline.utils.validateCid

internal class SendMessage(private val domainImpl: ChatDomainImpl) {
    /**
     * Sends the message. Immediately adds the message to local storage
     * API call to send the message is retried according to the retry policy specified on the chatDomain
     *
     * @param message The message to send.
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    operator fun invoke(
        message: Message,
    ): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        return CoroutineCall(domainImpl.scope) {
            val channelController = domainImpl.channel(cid)

            message.populateMentions(channelController.toChannel())

            if (message.replyMessageId != null) {
                channelController.replyMessage(null)
            }
            channelController.sendMessage(message)
        }
    }
}
