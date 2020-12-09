package io.getstream.chat.android.ui.messages.adapter

public enum class MessageListItemViewType(public val typeValue: Int) {
    DATE_DIVIDER(1),
    MESSAGE_DELETED(2),
    PLAIN_TEXT(3),
    REPLY_MESSAGE(4),
    PLAIN_TEXT_WITH_ATTACHMENTS(5),
    MEDIA_ATTACHMENTS(6),
    ATTACHMENTS(7),
    LOADING_INDICATOR(8),
    THREAD_SEPARATOR(9)
}
