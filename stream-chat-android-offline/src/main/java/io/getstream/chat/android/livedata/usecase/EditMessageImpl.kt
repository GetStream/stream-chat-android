package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface EditMessage {
    /**
     * Edits the specified message. Local storage is updated immediately
     * The API request is retried according to the retry policy specified on the chatDomain
     * @param message the message to edit
     * @return A call object with Message as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(message: Message): Call<Message>
}

internal class EditMessageImpl(private val domainImpl: ChatDomainImpl) : EditMessage {
    override operator fun invoke(message: Message): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        val runnable = suspend {
            channelController.editMessage(message)
        }
        return CallImpl2(runnable, channelController.scope)
    }
}
