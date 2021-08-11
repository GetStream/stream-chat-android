package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

internal data class AddDeviceRequest(
    @SerializedName("id") val token: String,
    @SerializedName("push_provider") val pushProvider: String,
)
