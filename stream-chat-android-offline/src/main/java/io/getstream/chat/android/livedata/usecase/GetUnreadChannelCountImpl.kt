package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import kotlinx.coroutines.flow.StateFlow
import io.getstream.chat.android.offline.usecase.GetUnreadChannelCount as OfflineGetUnreadChannelCount

@Deprecated(
    message = "Use ChatDomain::channelUnreadCount instead",
    level = DeprecationLevel.ERROR,
)
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
    public operator fun invoke(): Call<LiveData<Int>>
}

@Suppress("DEPRECATION_ERROR")
internal class GetUnreadChannelCountImpl(private val domainImpl: OfflineGetUnreadChannelCount) : GetUnreadChannelCount {
    override operator fun invoke(): Call<LiveData<Int>> = domainImpl.invoke().map(StateFlow<Int>::asLiveData)
}
