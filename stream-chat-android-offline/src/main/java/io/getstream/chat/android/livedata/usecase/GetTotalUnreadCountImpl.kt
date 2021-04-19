package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import kotlinx.coroutines.flow.StateFlow
import io.getstream.chat.android.offline.usecase.GetTotalUnreadCount as OfflineGetTotalUnreadCount

@Deprecated(
    message = "Use ChatDomain::totalUnreadCount instead",
    level = DeprecationLevel.WARNING,
)
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

internal class GetTotalUnreadCountImpl(private val offlineGetTotalUnreadCount: OfflineGetTotalUnreadCount) :
    GetTotalUnreadCount {
    override operator fun invoke(): Call<LiveData<Int>> =
        offlineGetTotalUnreadCount.invoke().map(StateFlow<Int>::asLiveData)
}
