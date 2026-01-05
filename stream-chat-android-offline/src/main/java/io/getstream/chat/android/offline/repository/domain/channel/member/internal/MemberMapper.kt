/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.channel.member.internal

import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User

internal fun Member.toEntity(): MemberEntity = MemberEntity(
    userId = getUserId(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited ?: false,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
    banned = banned,
    channelRole = channelRole,
    notificationsMuted = notificationsMuted,
    status = status,
    banExpires = banExpires,
    extraData = extraData,
    pinnedAt = pinnedAt,
    archivedAt = archivedAt,
)

internal suspend fun MemberEntity.toModel(getUser: suspend (userId: String) -> User): Member = Member(
    user = getUser(userId),
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
    banned = banned,
    channelRole = channelRole,
    notificationsMuted = notificationsMuted,
    status = status,
    banExpires = banExpires,
    pinnedAt = pinnedAt,
    archivedAt = archivedAt,
    extraData = extraData,
)
