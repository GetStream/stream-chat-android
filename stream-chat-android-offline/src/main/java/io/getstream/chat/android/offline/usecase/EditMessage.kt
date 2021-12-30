package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.extensions.populateMentions
import io.getstream.chat.android.offline.utils.validateCid

internal class EditMessage(private val domainImpl: ChatDomainImpl) {
    /**
     * Edits the specified message. Local storage is updated immediately
     * The API request is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to edit.
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    operator fun invoke(message: Message): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        return CoroutineCall(domainImpl.scope) {
            val channelController = domainImpl.channel(cid)

            message.populateMentions(channelController.toChannel())

            channelController.editMessage(message)
        }
    }
}
