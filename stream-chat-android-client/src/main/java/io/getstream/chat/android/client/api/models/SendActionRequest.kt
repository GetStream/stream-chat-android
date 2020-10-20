package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

public data class SendActionRequest(
    @SerializedName("channel_id")
    val channelId: String,
    @SerializedName("message_id")
    val messageId: String,
    val type: String,
    @SerializedName("form_data")
    val formData: Map<Any, Any>
)
