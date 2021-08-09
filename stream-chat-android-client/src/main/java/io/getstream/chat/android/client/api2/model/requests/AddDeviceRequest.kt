package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AddDeviceRequest(
    val id: String,
    val push_provider: String,
)
