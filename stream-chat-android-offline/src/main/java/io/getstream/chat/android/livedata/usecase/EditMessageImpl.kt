package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
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
    public operator fun invoke(message: Message): Call2<Message>
}

internal class EditMessageImpl(private val domainImpl: ChatDomainImpl) : EditMessage {
    override operator fun invoke(message: Message): Call2<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelRepo = domainImpl.channel(cid)

        val runnable = suspend {
            channelRepo.editMessage(message)
        }
        return CallImpl2(
            runnable,
            channelRepo.scope
        )
    }
}
