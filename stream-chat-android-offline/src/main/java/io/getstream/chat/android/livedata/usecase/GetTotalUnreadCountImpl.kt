package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl

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
    public operator fun invoke(): Call<LiveData<Int>>
}

internal class GetTotalUnreadCountImpl(private val domainImpl: ChatDomainImpl) : GetTotalUnreadCount {
    override operator fun invoke(): Call<LiveData<Int>> {
        return CoroutineCall(domainImpl.scope) {
            Result(domainImpl.totalUnreadCount)
        }
    }
}
