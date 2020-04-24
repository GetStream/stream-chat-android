package io.getstream.chat.android.livedata.usecase

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl

interface GetTotalUnreadCount {
    operator fun invoke(): Call2<LiveData<Int>>
}

class GetTotalUnreadCountImpl(var domainImpl: ChatDomainImpl) : GetTotalUnreadCount {
    override operator fun invoke(): Call2<LiveData<Int>> {
        var runnable = suspend {
            Result(domainImpl.totalUnreadCount, null)
        }
        return CallImpl2<LiveData<Int>>(runnable, domainImpl.scope)
    }
}
