package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class ThreadLoadMore(private val domainImpl: ChatDomainImpl) {
    /**
     * Loads more messages for the specified thread
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param parentId: the parentId of the thread
     * @param messageLimit: how many new messages to load
     */
    @CheckResult
    operator fun invoke(cid: String, parentId: String, messageLimit: Int): Call<List<Message>> {
        validateCid(cid)
        require(parentId.isNotEmpty()) { "parentId can't be empty" }

        val channelController = domainImpl.channel(cid)
        val threadController = channelController.getThread(parentId)

        return CoroutineCall(domainImpl.scope) {
            threadController.loadOlderMessages(messageLimit)
        }
    }
}
