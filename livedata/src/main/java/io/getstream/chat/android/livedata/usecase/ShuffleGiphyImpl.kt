package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface ShuffleGiphy {
    public operator fun invoke(message: Message): Call2<Message>
}

/**
 * Performs giphy shuffle operation. Removes the original "ephemeral" message from local storage.
 * Returns new "ephemeral" message with new giphy url.
 * API call to remove the message is retried according to the retry policy specified on the chatDomain
 * @param message the message to send
 * @return A call object with Message as the return type
 * @see io.getstream.chat.android.livedata.utils.RetryPolicy
 */
internal class ShuffleGiphyImpl(private val domainImpl: ChatDomainImpl) : ShuffleGiphy {
    override operator fun invoke(message: Message): Call2<Message> {
        val cid = message.cid
        validateCid(cid)

        val channelRepo = domainImpl.channel(cid)

        val runnable = suspend {
            channelRepo.shuffleGiphy(message)
        }

        return CallImpl2(runnable, channelRepo.scope)
    }
}
