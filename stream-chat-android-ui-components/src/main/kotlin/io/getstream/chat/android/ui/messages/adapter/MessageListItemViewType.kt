package io.getstream.chat.android.ui.messages.adapter

public enum class MessageListItemViewType(public val typeValue: Int) {
    DATE_DIVIDER(1),
    TYPING_INDICATOR(2),
    MESSAGE_DELETED(3),
    PLAIN_TEXT(4),
    REPLY_MESSAGE(5),
    PLAIN_TEXT_WITH_ATTACHMENTS(6),
    ATTACHMENTS(7),
    LOADING_INDICATOR(8),
    THREAD_SEPARATOR(9)
}
