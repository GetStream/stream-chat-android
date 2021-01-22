package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.validateCid

public interface LoadMessageById {
    /**
     * Loads message for a given message id and channel id
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param messageId: the id of the message
     * @param olderMessagesOffset: how many new messages to load before the requested message
     * @param newerMessagesOffset: how many new messages to load after the requested message
     */
    @CheckResult
    public operator fun invoke(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int = 0,
        newerMessagesOffset: Int = 0,
    ): Call<Message>
}

internal class LoadMessageByIdImpl(private val domainImpl: ChatDomainImpl) : LoadMessageById {
    override operator fun invoke(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Call<Message> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.loadMessageById(messageId, newerMessagesOffset, olderMessagesOffset)
        }
    }
}
