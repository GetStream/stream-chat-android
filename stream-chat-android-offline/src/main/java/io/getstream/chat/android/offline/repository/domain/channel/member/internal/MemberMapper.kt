@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.offline.repository.domain.channel.member.internal

import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User

internal fun Member.toEntity(): MemberEntity = MemberEntity(
    userId = getUserId(),
    role = role ?: user.role,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited ?: false,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
    banned = banned,
    channelRole = channelRole,
)

internal suspend fun MemberEntity.toModel(getUser: suspend (userId: String) -> User): Member = Member(
    user = getUser(userId),
    role = role,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
    banned = banned,
    channelRole = channelRole,
)
