package io.getstream.chat.android.client.debugger

import io.getstream.chat.android.client.models.Message

public interface ChatClientDebugger {

    public fun debugSendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
    ): SendMessageDebugger = StubSendMessageDebugger
}

internal object StubChatClientDebugger : ChatClientDebugger