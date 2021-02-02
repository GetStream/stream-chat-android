package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.DeviceDto

@JsonClass(generateAdapter = true)
internal data class DevicesResponse(
    val devices: List<DeviceDto>,
)
