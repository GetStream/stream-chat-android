package io.getstream.chat.android.compose.state.messages

import io.getstream.chat.android.client.models.Message

/**
 * Represents a state when a message or its reactions were clicked.
 *
 * @param message The selected message.
 */
public sealed class SelectedMessageState(public val message: Message)

/**
 * Represents a state when a message was clicked.
 */
public class SelectedMessageOptionsState(message: Message) : SelectedMessageState(message)

/**
 * Represents a state when message reactions were clicked.
 */
public class SelectedMessageReactionsState(message: Message) : SelectedMessageState(message)
