package io.getstream.chat.android.offline.event.handler.factory

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.event.handler.ChatEventHandler
import io.getstream.chat.android.offline.event.handler.DefaultChatEventHandler
import kotlinx.coroutines.flow.StateFlow

/**
* A [ChatEventHandler] factory. Allows passing visible channels` list.
*/
public open class ChatEventHandlerFactory {

    /**
     * Creates a [ChatEventHandler] instance.
     *
     * @param channels The visible channels` list.
     */
    public open fun chatEventHandler(channels: StateFlow<List<Channel>>): ChatEventHandler {
        return DefaultChatEventHandler(channels = channels)
    }
}
