package io.getstream.chat.android.client.api.models

public data class SendActionRequest(
    val channelId: String,
    val messageId: String,
    val type: String,
    val formData: Map<Any, Any>,
)
