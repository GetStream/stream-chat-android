package io.getstream.chat.android.core.poc.library.rest


class MarkReadRequest(messageId: String) {
    val event = MessageId(messageId)

    class MessageId(val message_id: String)
}