package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class LoadMessageById(private val domainImpl: ChatDomainImpl) {
    /**
     * Loads message for a given message id and channel id
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param messageId: the id of the message
     * @param olderMessagesOffset: how many new messages to load before the requested message
     * @param newerMessagesOffset: how many new messages to load after the requested message
     */
    @CheckResult
    operator fun invoke(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int = 0,
        newerMessagesOffset: Int = 0,
    ): Call<Message> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.loadMessageById(messageId, newerMessagesOffset, olderMessagesOffset)
        }
    }
}
