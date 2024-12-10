/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDto
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.UserId

internal fun DownstreamMemberDto.toDomain(currentUserId: UserId?): Member =
    Member(
        user = user.toDomain(currentUserId),
        createdAt = created_at,
        updatedAt = updated_at,
        isInvited = invited,
        inviteAcceptedAt = invite_accepted_at,
        inviteRejectedAt = invite_rejected_at,
        shadowBanned = shadow_banned ?: false,
        banned = banned ?: false,
        channelRole = channel_role,
        notificationsMuted = notifications_muted,
        status = status,
        banExpires = ban_expires,
        pinnedAt = pinned_at,
        archivedAt = archived_at,
        extraData = extraData,
    )

internal fun Member.toDto(): UpstreamMemberDto =
    UpstreamMemberDto(
        user = user.toDto(),
        created_at = createdAt,
        updated_at = updatedAt,
        invited = isInvited,
        invite_accepted_at = inviteAcceptedAt,
        invite_rejected_at = inviteRejectedAt,
        shadow_banned = shadowBanned,
        banned = banned,
        channel_role = channelRole,
        notifications_muted = notificationsMuted,
        status = status,
        ban_expires = banExpires,
        pinned_at = pinnedAt,
        archived_at = archivedAt,
        extraData = extraData,
    )
