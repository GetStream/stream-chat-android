package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.flow.StateFlow
import io.getstream.chat.android.offline.ChatDomainImpl as NewChatDomainImpl

public interface GetTotalUnreadCount {
    /**
     * Returns the total unread messages count for the current user.
     * You might also be interested in GetUnreadChannelCount
     * Or ChannelController.unreadCount
     *
     * @see io.getstream.chat.android.livedata.usecase.GetUnreadChannelCount
     * @see io.getstream.chat.android.livedata.controller.ChannelController.unreadCount
     */
    @CheckResult
    public operator fun invoke(): Call<StateFlow<Int>>
}

internal class GetTotalUnreadCountImpl(private val domainImpl: NewChatDomainImpl) : GetTotalUnreadCount {
    override operator fun invoke(): Call<StateFlow<Int>> {
        return CoroutineCall(domainImpl.scope) {
            Result(domainImpl.totalUnreadCount)
        }
    }
}
