package io.getstream.chat.android.compose.state.messages

sealed class ConversationTypingIndicatorState(val isShowing: Boolean)

class HeaderTypingIndicator(isShowing: Boolean) : ConversationTypingIndicatorState(isShowing)

class FooterTypingIndicator(isShowing: Boolean) : ConversationTypingIndicatorState(isShowing)