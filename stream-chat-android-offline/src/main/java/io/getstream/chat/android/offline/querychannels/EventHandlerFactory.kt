package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.StateFlow

public interface EventHandlerFactory {

    public fun chatEventHandler(channels: StateFlow<List<Channel>>): ChatEventHandler
}
