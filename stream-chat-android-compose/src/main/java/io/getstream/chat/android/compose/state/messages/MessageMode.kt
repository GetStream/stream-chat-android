package io.getstream.chat.android.compose.state.messages

import io.getstream.chat.android.client.models.Message

/**
 * Represents the message mode that's currently active.
 *
 * [Normal] - Regular mode, conversation with other users.
 * [Thread] - Thread mode, where there's a parent message to respond to.
 * */
public sealed class MessageMode

public object Normal : MessageMode()

/**
 * @param parentMessage - The message users are responding to in a Thread.
 * */
public class Thread(public val parentMessage: Message) : MessageMode()
