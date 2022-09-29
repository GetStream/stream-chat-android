package io.getstream.chat.android.client.channel

import io.getstream.chat.android.client.channel.state.ChannelMessagesState
import io.getstream.chat.android.client.models.Message

public interface ChannelMessagesUpdateLogic {

    public fun upsertMessage(message: Message)

    public fun upsertMessages(messages: List<Message>, shouldRefreshMessages: Boolean = false)

    public fun listenForChannelState(): ChannelMessagesState
}
