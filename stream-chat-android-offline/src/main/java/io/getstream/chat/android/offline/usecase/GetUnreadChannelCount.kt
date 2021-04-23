package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import kotlinx.coroutines.flow.StateFlow

@Deprecated(
    message = "Use ChatDomain::channelUnreadCount instead",
    level = DeprecationLevel.WARNING,
)
public class GetUnreadChannelCount internal constructor(private val domainImpl: ChatDomainImpl) {
    /**
     * Returns the number of channels with unread messages for the given user.
     * You might also be interested in GetTotalUnreadCount
     * Or ChannelController.unreadCount
     *
     * @see io.getstream.chat.android.offline.usecase.GetTotalUnreadCount
     * @see io.getstream.chat.android.offline.channel.ChannelController.unreadCount
     */
    @CheckResult
    public operator fun invoke(): Call<StateFlow<Int>> {
        return CoroutineCall(domainImpl.scope) {
            Result(domainImpl.channelUnreadCount)
        }
    }
}
