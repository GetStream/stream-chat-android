package io.getstream.chat.android.client.rest


data class SendActionRequest(
    val channelId: String,
    val messageId: String,
    val type: String,
    val form_data: Map<Any, Any>
)