package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.CoroutineCall
import io.getstream.chat.android.livedata.utils.validateCid

public interface DeleteMessage {
    /**
     * Deletes the specified message, request is retried according to the retry policy specified on the chatDomain
     * @param message the message to mark as deleted
     * @return A call object with Message as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(message: Message): Call<Message>
}

internal class DeleteMessageImpl(private val domainImpl: ChatDomainImpl) : DeleteMessage {
    override operator fun invoke(message: Message): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        val runnable = suspend {
            channelController.deleteMessage(message)
        }
        return CoroutineCall(domainImpl.scope, runnable)
    }
}
