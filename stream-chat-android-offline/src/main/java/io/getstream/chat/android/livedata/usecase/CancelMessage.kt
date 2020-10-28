package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.CoroutineCall
import io.getstream.chat.android.livedata.utils.validateCid

public interface CancelMessage {
    /**
     * Cancels the message of "ephemeral" type. Removes the message from local storage.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @return A call object with Message as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(message: Message): Call<Boolean>
}

internal class CancelMessageImpl(private val domainImpl: ChatDomainImpl) : CancelMessage {
    override operator fun invoke(message: Message): Call<Boolean> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        val runnable = suspend {
            channelController.cancelMessage(message)
        }
        return CoroutineCall(domainImpl.scope, runnable)
    }
}
