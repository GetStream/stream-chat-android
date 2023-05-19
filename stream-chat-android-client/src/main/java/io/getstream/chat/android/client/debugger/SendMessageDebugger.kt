package io.getstream.chat.android.client.debugger

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

/**
 * Debugs the sending message flow.
 */
public interface SendMessageDebugger {
    /**
     * Called when the sending of a message starts.
     *
     * @param message The message being sent.
     */
    public fun onStart(message: Message) {}

    /**
     * Called when the interception of a message starts.
     *
     * @param message The message being intercepted.
     */
    public fun onInterceptionStart(message: Message) {}

    /**
     * Called when an intercepted message is updated during interception.
     *
     * @param message The updated message.
     */
    public fun onInterceptionUpdate(message: Message) {}

    /**
     * Called when the interception of a message is stopped and the result is available.
     *
     * @param result The result of the intercepted message.
     */
    public fun onInterceptionStop(result: Result<Message>) {}

    /**
     * Called when the sending of a message starts after interception.
     *
     * @param message The message being sent.
     */
    public fun onSendStart(message: Message) {}

    /**
     * Called when the sending of a message is stopped and the result is available.
     *
     * @param result The result of the sent message.
     */
    public fun onSendStop(result: Result<Message>) {}

    /**
     * Called when the sending of a message is completely stopped and the final result is available.
     *
     * @param result The final result of the message.
     */
    public fun onStop(result: Result<Message>) {}
}

internal object StubSendMessageDebugger : SendMessageDebugger