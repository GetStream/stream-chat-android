package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName

internal data class GetDevicesResponse(val devices: List<DeviceReponse> = emptyList())
internal data class DeviceReponse(
    @SerializedName("id") val token: String,
    @SerializedName("push_provider") var pushProvider: String,
)
