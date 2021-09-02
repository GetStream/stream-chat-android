package io.getstream.chat.android.client.api.models

internal data class GetDevicesResponse(val devices: List<DeviceReponse> = emptyList())

internal data class DeviceReponse(
    val token: String,
    var pushProvider: String,
)
