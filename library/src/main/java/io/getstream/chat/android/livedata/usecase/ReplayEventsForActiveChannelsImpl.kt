package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.utils.Call2
import io.getstream.chat.android.livedata.utils.CallImpl2

interface ReplayEventsForActiveChannels {
    operator fun invoke(cid: String): Call2<List<ChatEvent>>
}

class ReplayEventsForActiveChannelsImpl(var domainImpl: ChatDomainImpl) : ReplayEventsForActiveChannels {
    override operator fun invoke(cid: String): Call2<List<ChatEvent>> {
        var runnable = suspend {
            domainImpl.replayEventsForActiveChannels(cid)
        }
        return CallImpl2<List<ChatEvent>>(
            runnable,
            domainImpl.scope
        )
    }
}
