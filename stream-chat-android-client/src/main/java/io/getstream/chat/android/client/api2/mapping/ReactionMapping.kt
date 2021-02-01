package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.ReactionDto
import io.getstream.chat.android.client.models.Reaction

internal fun Reaction.toDto(): ReactionDto =
    ReactionDto(
        message_id = messageId,
        type = type,
        score = score,
        user = user?.toDto(),
        user_id = userId,
        created_at = createdAt,
        updated_at = updatedAt,
    )

internal fun ReactionDto.toDomain(): Reaction =
    Reaction(
        messageId = message_id,
        type = type,
        score = score,
        user = user?.toDomain(),
        userId = user_id,
        createdAt = created_at,
        updatedAt = updated_at,
    )
