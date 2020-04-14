package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatRepo
import java.security.InvalidParameterException

class DeleteReaction(var repo: ChatRepo) {
    operator fun invoke (cid: String, reaction: Reaction): Call2<Reaction> {
        var runnable = suspend {
            if (cid.isEmpty()) {
                throw InvalidParameterException("message.cid cant be empty")
            }

            val channelRepo = repo.channel(cid)
            channelRepo.deleteReaction(reaction)
        }
        return CallImpl2(runnable)
    }
}