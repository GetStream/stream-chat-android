package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.CoroutineCall
import io.getstream.chat.android.livedata.utils.validateCid

public interface SendGiphy {
    /**
     * Sends selected giphy message to the channel. Removes the original "ephemeral" message from local storage.
     * Returns new "ephemeral" message with new giphy url.
     * API call to remove the message is retried according to the retry policy specified on the chatDomain
     * @param message the message to send
     * @return A call object with Message as the return type
     * @see io.getstream.chat.android.livedata.utils.RetryPolicy
     */
    public operator fun invoke(message: Message): Call<Message>
}

internal class SendGiphyImpl(private val domainImpl: ChatDomainImpl) : SendGiphy {
    override operator fun invoke(message: Message): Call<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        val runnable = suspend {
            channelController.sendGiphy(message)
        }
        return CoroutineCall(domainImpl.scope, runnable)
    }
}
