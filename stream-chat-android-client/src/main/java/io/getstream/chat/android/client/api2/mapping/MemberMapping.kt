package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDto
import io.getstream.chat.android.client.models.Member

internal fun DownstreamMemberDto.toDomain(): Member =
    Member(
        user = user.toDomain(),
        role = role,
        createdAt = created_at,
        updatedAt = updated_at,
        isInvited = invited,
        inviteAcceptedAt = invite_accepted_at,
        inviteRejectedAt = invite_rejected_at,
        shadowBanned = shadow_banned,
    )

internal fun Member.toDto(): UpstreamMemberDto =
    UpstreamMemberDto(
        user = user.toDto(),
        role = role,
        created_at = createdAt,
        updated_at = updatedAt,
        invited = isInvited,
        invite_accepted_at = inviteAcceptedAt,
        invite_rejected_at = inviteRejectedAt,
        shadow_banned = shadowBanned,
    )
