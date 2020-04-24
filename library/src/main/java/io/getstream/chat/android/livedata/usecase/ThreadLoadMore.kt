package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomain
import java.security.InvalidParameterException

class ThreadLoadMore(var domain: ChatDomain) {
    operator fun invoke(cid: String, parentId: String, messageLimit: Int): Call2<List<Message>> {
        if (cid.isEmpty()) {
            throw InvalidParameterException("cid cant be empty")
        }
        if (parentId.isEmpty()) {
            throw InvalidParameterException("parentId cant be empty")
        }

        val channelController = domain.channel(cid)
        val threadController = channelController.getThread(parentId)

        val runnable = suspend {
            threadController.loadOlderMessages(messageLimit)
        }
        return CallImpl2(runnable, channelController.scope)
    }
}
