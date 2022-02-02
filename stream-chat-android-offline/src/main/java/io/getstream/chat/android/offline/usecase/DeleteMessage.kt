package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

@Deprecated(
    message = "DeleteMessage is deprecated. Use function ChatClient::deleteMessage instead",
    replaceWith = ReplaceWith(
        expression = "ChatClient.instance().deleteMessage(message)",
        imports = arrayOf("io.getstream.chat.android.client.ChatClient")
    ),
    level = DeprecationLevel.WARNING
)
internal class DeleteMessage(private val domainImpl: ChatDomainImpl) {
    /**
     * Deletes the specified message, request is retried according to the retry policy specified on the chatDomain.
     *
     * @param message The message to mark as deleted.
     * @see io.getstream.chat.android.offline.utils.RetryPolicy
     */
    @CheckResult
    operator fun invoke(message: Message, hard: Boolean = false): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.deleteMessage(message, hard)
        }
    }
}
