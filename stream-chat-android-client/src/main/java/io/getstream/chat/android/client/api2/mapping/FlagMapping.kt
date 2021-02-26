package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamFlagDto
import io.getstream.chat.android.client.models.Flag

internal fun DownstreamFlagDto.toDomain(): Flag {
    return Flag(
        user = user.toDomain(),
        targetUser = target_user?.toDomain(),
        targetMessageId = target_message_id,
        reviewedBy = created_at,
        createdByAutomod = created_by_automod,
        createdAt = approved_at,
        updatedAt = updated_at,
        reviewedAt = reviewed_at,
        approvedAt = reviewed_by,
        rejectedAt = rejected_at,
    )
}
