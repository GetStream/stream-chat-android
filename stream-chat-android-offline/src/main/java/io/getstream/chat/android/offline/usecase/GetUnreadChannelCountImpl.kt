package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.flow.StateFlow
import io.getstream.chat.android.offline.ChatDomainImpl as NewChatDomainImpl

public interface GetUnreadChannelCount {
    /**
     * Returns the number of channels with unread messages for the given user.
     * You might also be interested in GetTotalUnreadCount
     * Or ChannelController.unreadCount
     *
     * @see io.getstream.chat.android.livedata.usecase.GetTotalUnreadCount
     * @see io.getstream.chat.android.livedata.controller.ChannelController.unreadCount
     */
    @CheckResult
    public operator fun invoke(): Call<StateFlow<Int>>
}

internal class GetUnreadChannelCountImpl(private val domainImpl: NewChatDomainImpl) : GetUnreadChannelCount {
    override operator fun invoke(): Call<StateFlow<Int>> {
        return CoroutineCall(domainImpl.scope) {
            Result(domainImpl.channelUnreadCount)
        }
    }
}
