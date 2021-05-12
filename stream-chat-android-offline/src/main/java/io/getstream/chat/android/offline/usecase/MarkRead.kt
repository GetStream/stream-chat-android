package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.utils.validateCid

internal class MarkRead(private val domainImpl: ChatDomainImpl) {
    /**
     * Marks the messages on the specified channel as read
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @return True if the mark read event was sent. False if there was no need to mark read
     *         (i. e. the messages are already marked as read).
     */
    @CheckResult
    operator fun invoke(cid: String): Call<Boolean> {
        val channelController = domainImpl.channel(validateCid(cid))

        return CoroutineCall(domainImpl.scope) {
            channelController.markRead().let { markedRead ->
                if (markedRead) {
                    domainImpl.client
                        .markRead(channelController.channelType, channelController.channelId)
                        .execute()
                }

                Result(markedRead)
            }
        }
    }
}
