package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.models.Device

internal fun Device.toDto(): DeviceDto =
    DeviceDto(
        id = id,
        push_provider = pushProvider,
    )

internal fun DeviceDto.toDomain(): Device =
    Device(
        id = id,
        pushProvider = push_provider,
    )
