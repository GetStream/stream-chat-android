package io.getstream.chat.android.client.api.models

internal data class AddDeviceRequest(
    val token: String,
    val pushProvider: String,
)
