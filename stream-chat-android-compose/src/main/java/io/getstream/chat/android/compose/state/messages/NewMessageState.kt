package io.getstream.chat.android.compose.state.messages

/**
 * Represents the state when a new message arrives to the channel.
 *
 * [MyOwn] - If the message is our own (we sent it), we scroll to the bottom of the list.
 * [Other] - If the message is someone else's (we didn't send it), we show a "New message" bubble.
 * */
sealed class NewMessageState

object MyOwn : NewMessageState()

object Other : NewMessageState()
