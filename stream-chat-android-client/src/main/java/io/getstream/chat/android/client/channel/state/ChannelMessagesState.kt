package io.getstream.chat.android.client.channel.state

import io.getstream.chat.android.client.models.Message
import kotlinx.coroutines.flow.StateFlow

public interface ChannelMessagesState {

    /** The message collection of this channel. */
    public val messages: StateFlow<List<Message>>
}
