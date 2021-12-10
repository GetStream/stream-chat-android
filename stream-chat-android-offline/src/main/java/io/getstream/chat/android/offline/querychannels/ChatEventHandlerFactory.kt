package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.StateFlow

public open class ChatEventHandlerFactory {

    public open fun chatEventHandler(channels: StateFlow<List<Channel>>): ChatEventHandler {
        return DefaultChatEventHandler(channels = channels)
    }
}
