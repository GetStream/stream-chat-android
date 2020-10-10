package io.getstream.chat.android.livedata.usecase

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2

interface GetTotalUnreadCount {
    /**
     * Returns the total unread messages count for the current user.
     * You might also be interested in GetUnreadChannelCount
     * Or ChannelController.unreadCount
     *
     * @return A call object with LiveData<Int> as the return type
     * @see io.getstream.chat.android.livedata.usecase.GetUnreadChannelCount
     * @see io.getstream.chat.android.livedata.controller.ChannelController.unreadCount
     */
    operator fun invoke(): Call2<LiveData<Int>>
}

class GetTotalUnreadCountImpl(var domainImpl: ChatDomainImpl) : GetTotalUnreadCount {
    override operator fun invoke(): Call2<LiveData<Int>> {
        val runnable = suspend {
            Result(domainImpl.totalUnreadCount, null)
        }
        return CallImpl2(
            runnable,
            domainImpl.scope
        )
    }
}
