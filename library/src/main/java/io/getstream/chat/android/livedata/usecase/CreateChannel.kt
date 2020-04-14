package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatRepo
import java.security.InvalidParameterException

class CreateChannel(var repo: ChatRepo) {
    operator fun invoke (channel: Channel): Call2<Channel> {
        var runnable = suspend {
            repo._createChannel(channel)
        }
        return CallImpl2<Channel>(runnable)
    }
}