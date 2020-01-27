package io.getstream.chat.android.core.poc.library.rest


class SendActionRequest(
    val channelId: String,
    val messageId: String,
    val type: String,
    val form_data: Map<Any, Any>
)