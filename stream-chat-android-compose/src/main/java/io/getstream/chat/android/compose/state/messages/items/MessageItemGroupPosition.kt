package io.getstream.chat.android.compose.state.messages.items

sealed class MessageItemGroupPosition

object Top : MessageItemGroupPosition()

object Middle : MessageItemGroupPosition()

object Bottom : MessageItemGroupPosition()

object None : MessageItemGroupPosition()
