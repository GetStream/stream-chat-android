package io.getstream.chat.android.client.api.models


data class SendActionRequest(
    val channelId: String,
    val messageId: String,
    val type: String,
    val form_data: Map<Any, Any>
)