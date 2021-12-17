package io.getstream.chat.android.compose.state.messages.list

import io.getstream.chat.android.client.models.Message

/**
 * Represents the list of actions users can take with ephemeral giphy messages.
 *
 * @param message The ephemeral giphy message.
 */
public sealed class GiphyAction(public val message: Message)

/**
 * Send the selected giphy message to the channel.
 */
public class SendGiphy(message: Message) : GiphyAction(message)

/**
 * Perform the giphy shuffle operation.
 */
public class ShuffleGiphy(message: Message) : GiphyAction(message)

/**
 * Cancel the ephemeral message.
 */
public class CancelGiphy(message: Message) : GiphyAction(message)
