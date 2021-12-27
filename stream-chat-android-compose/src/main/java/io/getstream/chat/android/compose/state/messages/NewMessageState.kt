package io.getstream.chat.android.compose.state.messages

/**
 * Represents the state when a new message arrives to the channel.
 */
public sealed class NewMessageState

/**
 * If the message is our own (we sent it), we scroll to the bottom of the list.
 */
public object MyOwn : NewMessageState()

/**
 * If the message is someone else's (we didn't send it), we show a "New message" bubble.
 */
public object Other : NewMessageState()
