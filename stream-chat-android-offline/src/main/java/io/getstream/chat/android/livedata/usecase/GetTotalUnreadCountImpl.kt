package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import kotlinx.coroutines.CoroutineScope

@Deprecated(
    message = "Use ChatDomain::totalUnreadCount instead",
    level = DeprecationLevel.ERROR,
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

@Suppress("DEPRECATION_ERROR")
internal class GetTotalUnreadCountImpl(private val chatDomain: ChatDomain, private val scope: CoroutineScope) :
    GetTotalUnreadCount {
    override operator fun invoke(): Call<LiveData<Int>> = CoroutineCall(scope) {
        Result(chatDomain.totalUnreadCount)
    }
}
