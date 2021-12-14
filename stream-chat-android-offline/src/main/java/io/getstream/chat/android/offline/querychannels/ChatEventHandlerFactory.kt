package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.StateFlow

/*
* This factory instantiates ChatEventHandler populating it with StateFlow<List<Channel>>
*/
public open class ChatEventHandlerFactory {

    public open fun chatEventHandler(channels: List<Channel>): ChatEventHandler {
        return DefaultChatEventHandler(channels = channels)
    }
}
