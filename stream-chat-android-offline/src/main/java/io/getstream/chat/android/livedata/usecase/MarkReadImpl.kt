package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.validateCid

public interface MarkRead {
    /**
     * Marks the messages on the specified channel as read
     *
     * @param cid: the full channel id i. e. messaging:123
     *
     * @return True if the mark read event was sent. False if there was no need to mark read
     *         (i. e. the messages are already marked as read).
     */
    @CheckResult
    public operator fun invoke(cid: String): Call<Boolean>
}

internal class MarkReadImpl(private val domainImpl: ChatDomainImpl) : MarkRead {
    override operator fun invoke(cid: String): Call<Boolean> {
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
