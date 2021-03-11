package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.utils.validateCid
import io.getstream.chat.android.offline.ChatDomainImpl as NewChatDomainImpl

public interface EditMessage {
    /**
     * Edits the specified message. Local storage is updated immediately
     * The API request is retried according to the retry policy specified on the chatDomain
     * @param message the message to edit
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    @CheckResult
    public operator fun invoke(message: Message): Call<Message>
}

internal class EditMessageImpl(private val domainImpl: NewChatDomainImpl) : EditMessage {
    override operator fun invoke(message: Message): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.editMessage(message)
        }
    }
}
