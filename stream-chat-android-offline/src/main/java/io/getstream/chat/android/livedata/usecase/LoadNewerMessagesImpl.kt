package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2
import io.getstream.chat.android.livedata.utils.validateCid

public interface LoadNewerMessages {
    /**
     * Loads newer messages for the channel
     *
     * @param cid: the full channel id IE messaging:123
     * @param messageLimit: how many new messages to load
     *
     * @return A call object with Channel as the return type
     */
    public operator fun invoke(cid: String, messageLimit: Int): Call2<Channel>
}

internal class LoadNewerMessagesImpl(private val domainImpl: ChatDomainImpl) : LoadNewerMessages {
    override operator fun invoke(cid: String, messageLimit: Int): Call2<Channel> {
        validateCid(cid)
        val channelRepo = domainImpl.channel(cid)
        val runnable = suspend {
            channelRepo.loadNewerMessages(messageLimit)
        }
        return CallImpl2(
            runnable,
            channelRepo.scope
        )
    }
}
