package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.MuteDto
import io.getstream.chat.android.client.models.Mute

internal fun Mute.toDto(): MuteDto =
    MuteDto(
        user = user.toDto(),
        target = user.toDto(),
        created_at = createdAt,
        updated_at = updatedAt,
    )

internal fun MuteDto.toDomain(): Mute =
    Mute(
        user = user.toDomain(),
        target = user.toDomain(),
        createdAt = created_at,
        updatedAt = updated_at,
    )
