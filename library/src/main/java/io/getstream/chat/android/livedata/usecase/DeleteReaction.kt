package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomain
import java.security.InvalidParameterException

class DeleteReaction(var domain: ChatDomain) {
    operator fun invoke(cid: String, reaction: Reaction): Call2<Message> {
        if (cid.isEmpty()) {
            throw InvalidParameterException("cid cant be empty")
        }

        val channelRepo = domain.channel(cid)

        var runnable = suspend {

            channelRepo.deleteReaction(reaction)
        }
        return CallImpl2(runnable, channelRepo.scope)
    }
}
