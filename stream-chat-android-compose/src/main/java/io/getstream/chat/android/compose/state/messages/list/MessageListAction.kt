package io.getstream.chat.android.compose.state.messages.list

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction

/**
 * Represents the list of actions users can take with selected messages.
 *
 * @param message The selected message.
 */
public sealed class MessageAction(public val message: Message)

/**
 * Add/remove a reaction on a message.
 *
 * @param reaction The reaction to add or remove from the message.
 */
public class React(
    public val reaction: Reaction,
    message: Message,
) : MessageAction(message)

/**
 * Start a message reply.
 */
public class Reply(message: Message) : MessageAction(message)

/**
 * Start a thread reply.
 */
public class ThreadReply(message: Message) : MessageAction(message)

/**
 * Copy the message content.
 */
public class Copy(message: Message) : MessageAction(message)

/**
 * Start editing an owned message.
 */
public class Edit(message: Message) : MessageAction(message)

/**
 * Show a delete dialog for owned message.
 */
public class Delete(message: Message) : MessageAction(message)

/**
 * Show a flag dialog for a message.
 */
public class Flag(message: Message) : MessageAction(message)

/**
 * Show a mute user dialog, for another user.
 */
public class MuteUser(message: Message) : MessageAction(message)

/**
 * User-customizable action, with any number of extra properties.
 *
 * @param extraProperties Map of key-value pairs that let you store extra data for this action.
 */
public class CustomAction(
    message: Message,
    public val extraProperties: Map<String, Any> = emptyMap(),
) : MessageAction(message)
