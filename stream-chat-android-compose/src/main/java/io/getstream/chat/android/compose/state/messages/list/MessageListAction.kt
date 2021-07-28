package io.getstream.chat.android.compose.state.messages.list

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction

/**
 * Represents the list of actions users can take with selected messages.
 *
 * @param message - The selected message.
 *
 * [React] - Add/remove a reaction on a message.
 * [Reply] - Start a message reply.
 * [ThreadReply] - Start a thread reply.
 * [Copy] - Copy the message content.
 * [Edit] - Start editing an owned message.
 * [Delete] - Show a delete dialog for owned message.
 * [Flag] - Show a flag dialog for a message.
 * [MuteUser] - Show a mute user dialog, for another user.
 * [CustomAction] - User-customizable action, with a map of properties they can define.
 * */
public sealed class MessageAction(public val message: Message)

/**
 * @param reaction - The reaction to add or remove from the message.
 * */
public class React(
    public val reaction: Reaction,
    message: Message,
) : MessageAction(message)

public class Reply(message: Message) : MessageAction(message)

public class ThreadReply(message: Message) : MessageAction(message)

public class Copy(message: Message) : MessageAction(message)

public class Edit(message: Message) : MessageAction(message)

public class Delete(message: Message) : MessageAction(message)

public class Flag(message: Message) : MessageAction(message)

public class MuteUser(message: Message) : MessageAction(message)

/**
 * Used to define any custom actions you need, with any number of extra properties.
 *
 * @param extraProperties - Map of key-value pairs that let you store extra data for this action.
 * */
public class CustomAction(
    message: Message,
    public val extraProperties: Map<String, Any> = emptyMap(),
) : MessageAction(message)
