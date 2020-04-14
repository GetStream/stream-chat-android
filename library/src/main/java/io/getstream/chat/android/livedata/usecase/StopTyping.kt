package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatRepo
import java.security.InvalidParameterException

class StopTyping(var repo: ChatRepo) {
    operator fun invoke (cid: String): Call2<Boolean> {
        var runnable = suspend {
            if (cid.isEmpty()) {
                throw InvalidParameterException("cid cant be empty")
            }

            val channelRepo = repo.channel(cid)
            channelRepo.stopTyping()
        }
        return CallImpl2<Boolean>(runnable)
    }
}