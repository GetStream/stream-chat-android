package io.getstream.chat.android.livedata.usecase

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl

interface GetUnreadChannelCount {
    operator fun invoke(): Call2<LiveData<Int>>
}

class GetUnreadChannelCountImpl(var domainImpl: ChatDomainImpl) : GetUnreadChannelCount {
    override operator fun invoke(): Call2<LiveData<Int>> {
        var runnable = suspend {
            Result(domainImpl.channelUnreadCount, null)
        }
        return CallImpl2<LiveData<Int>>(
            runnable,
            domainImpl.scope
        )
    }
}
