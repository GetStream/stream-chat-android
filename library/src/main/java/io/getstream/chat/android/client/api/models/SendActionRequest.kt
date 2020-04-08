package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName


data class SendActionRequest(
    val channelId: String,
    val messageId: String,
    val type: String,
    @SerializedName("form_data")
    val formData: Map<Any, Any>
)