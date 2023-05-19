package io.getstream.chat.android.client.debugger

import io.getstream.chat.android.client.models.Message

/**
 * Debugs the [io.getstream.chat.android.client.ChatClient].
 */
public interface ChatClientDebugger {

    /**
     * Creates an instance of [SendMessageDebugger] that allows you to debug the sending process of a message.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param message Message object
     * @param isRetrying True if this message is being retried.
     *
     * @return Your custom [SendMessageDebugger] implementation.
     */
    public fun debugSendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
    ): SendMessageDebugger = StubSendMessageDebugger
}

/**
 * Mock [ChatClientDebugger] implementation.
 */
internal object StubChatClientDebugger : ChatClientDebugger