package io.getstream.chat.android.compose.state.messages.items

public sealed class MessageItemGroupPosition

public object Top : MessageItemGroupPosition()

public object Middle : MessageItemGroupPosition()

public object Bottom : MessageItemGroupPosition()

public object None : MessageItemGroupPosition()
