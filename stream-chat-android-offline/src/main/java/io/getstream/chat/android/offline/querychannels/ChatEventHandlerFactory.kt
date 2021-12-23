package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.StateFlow

/**
* A [ChatEventHandler] factory. Allows to pass visible channels` list
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
