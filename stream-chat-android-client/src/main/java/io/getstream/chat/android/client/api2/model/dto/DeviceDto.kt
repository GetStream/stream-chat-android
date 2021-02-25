package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class DeviceDto(
    val id: String,
    val push_provider: String,
)
