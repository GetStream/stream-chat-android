package io.getstream.chat.android.compose.state.messages

import io.getstream.chat.android.client.models.Message

/**
 * Represents a state when a message or its reactions were selected.
 *
 * @param message The selected message.
 */
public sealed class SelectedMessageState(public val message: Message)

/**
 * Represents a state when a message was selected.
 */
public class SelectedMessageOptionsState(message: Message) : SelectedMessageState(message)

/**
 * Represents a state when message reactions were selected.
 */
public class SelectedMessageReactionsState(message: Message) : SelectedMessageState(message)

/**
 * Represents a state when the show more reactions button was clicked.
 */
public class SelectedMessageReactionsPickerState(message: Message) : SelectedMessageState(message)
