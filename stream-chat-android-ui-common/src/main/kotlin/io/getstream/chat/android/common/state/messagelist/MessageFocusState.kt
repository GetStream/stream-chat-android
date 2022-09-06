package io.getstream.chat.android.common.state.messagelist

/**
 * Represents the message focus state, in case the user jumps to a message.
 */
public sealed class MessageFocusState

/**
 * Represents the state when the message is currently being focused.
 */
public object MessageFocused : MessageFocusState()

/**
 * Represents the state when we've removed the focus from the message.
 */
public object MessageFocusRemoved : MessageFocusState()