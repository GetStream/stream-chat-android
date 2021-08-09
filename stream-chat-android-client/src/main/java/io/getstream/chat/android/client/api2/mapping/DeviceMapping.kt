package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider

internal fun Device.toDto(): DeviceDto =
    DeviceDto(
        id = token,
        push_provider = pushProvider.key,
    )

internal fun DeviceDto.toDomain(): Device =
    Device(
        token = id,
        pushProvider = PushProvider.fromKey(push_provider),
    )
