package io.getstream.chat.android.compose.state.messages

import io.getstream.chat.android.client.models.Message

/**
 * Represents a state when a message component was selected.
 *
 * @param message The selected message.
 */
public sealed class SelectedMessageState(public val message: Message)

/**
 * Represents a state when a user clicked on a message item in the message list.
 */
public class SelectedMessageOptionsState(message: Message) : SelectedMessageState(message)

/**
 * Represents a state when a user clicked on message reactions in the message list.
 */
public class SelectedMessageReactionsState(message: Message) : SelectedMessageState(message)
