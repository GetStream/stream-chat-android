package io.getstream.chat.android.compose.state.messages

import io.getstream.chat.android.client.models.Message

/**
 * Represents a state when a message or its reactions were selected.
 *
 * @param message The selected message.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
 */
public sealed class SelectedMessageState(public val message: Message, public val ownCapabilities: Set<String>)

/**
 * Represents a state when a message was selected.
 */
public class SelectedMessageOptionsState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

/**
 * Represents a state when message reactions were selected.
 */
public class SelectedMessageReactionsState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)

/**
 * Represents a state when the show more reactions button was clicked.
 */
public class SelectedMessageReactionsPickerState(message: Message, ownCapabilities: Set<String>) :
    SelectedMessageState(message, ownCapabilities)
