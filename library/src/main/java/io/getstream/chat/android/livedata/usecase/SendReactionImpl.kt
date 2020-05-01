package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import java.security.InvalidParameterException

interface SendReaction {
    operator fun invoke(cid: String, reaction: Reaction): Call2<Reaction>
}

class SendReactionImpl(var domainImpl: ChatDomainImpl) : SendReaction {
    override operator fun invoke(cid: String, reaction: Reaction): Call2<Reaction> {
        if (cid.isEmpty()) {
            throw InvalidParameterException("message.cid cant be empty")
        }

        val channelRepo = domainImpl.channel(cid)

        var runnable = suspend {

            channelRepo.sendReaction(reaction)
        }
        return CallImpl2(
            runnable,
            channelRepo.scope
        )
    }
}
