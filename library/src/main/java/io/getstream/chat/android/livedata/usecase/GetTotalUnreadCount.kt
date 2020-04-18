package io.getstream.chat.android.livedata.usecase

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomain

class GetTotalUnreadCount(var domain: ChatDomain) {
    operator fun invoke (): Call2<LiveData<Int>> {
        var runnable = suspend {
            Result(domain.totalUnreadCount, null)
        }
        return CallImpl2<LiveData<Int>>(runnable)
    }
}