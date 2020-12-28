package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.validateCid

public interface ReplyMessage {
    public operator fun invoke(message: Message): Call<Unit>
}

internal class ReplyMessageImpl(private val chatDomain: ChatDomainImpl) : ReplyMessage {
    override fun invoke(message: Message): Call<Unit> {
        validateCid(message.cid)

        val channelController = chatDomain.channel(message.cid)
        return CoroutineCall(chatDomain.scope) {
            channelController.replyMessage(message)
        }
    }
}
