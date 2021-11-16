package io.getstream.chat.android.common.state

import io.getstream.chat.android.client.models.Message

/**
 * Represents the message mode that's currently active.
 */
public sealed class MessageMode {

    /**
     * Regular mode, conversation with other users.
     */
    public object Normal : MessageMode()

    /**
     * Thread mode, where there's a parent message to respond to.
     *
     * @param parentMessage The message users are responding to in a Thread.
     */
    public class MessageThread(public val parentMessage: Message) : MessageMode()
}
