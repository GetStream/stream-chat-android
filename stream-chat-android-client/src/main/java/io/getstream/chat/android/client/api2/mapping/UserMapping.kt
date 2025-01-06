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

import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserBlockDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.api2.model.response.BlockUserResponse
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.models.UserId

internal fun User.toDto(): UpstreamUserDto =
    UpstreamUserDto(
        banned = isBanned,
        id = id,
        name = name,
        image = image,
        invisible = isInvisible,
        privacy_settings = privacySettings?.toDto(),
        language = language,
        role = role,
        devices = devices.map(Device::toDto),
        teams = teams,
        extraData = extraData,
    )

internal fun DownstreamUserDto.toDomain(currentUserId: UserId?): User =
    User(
        id = id,
        name = name ?: "",
        image = image ?: "",
        role = role,
        invisible = invisible,
        language = language ?: "",
        banned = banned,
        devices = devices.orEmpty().map(DeviceDto::toDomain),
        online = online,
        createdAt = created_at,
        deactivatedAt = deactivated_at,
        updatedAt = updated_at,
        lastActive = last_active,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        unreadThreads = unread_threads,
        mutes = mutes.orEmpty().map { it.toDomain(currentUserId) },
        teams = teams,
        channelMutes = channel_mutes.orEmpty().map { it.toDomain(currentUserId) },
        blockedUserIds = blocked_user_ids.orEmpty(),
        extraData = extraData.toMutableMap(),
    )

internal fun DownstreamUserBlockDto.toDomain(): UserBlock = UserBlock(
    blockedBy = user_id,
    userId = blocked_user_id,
    blockedAt = created_at,
)

internal fun List<DownstreamUserBlockDto>.toDomain(): List<UserBlock> = map { it.toDomain() }

internal fun BlockUserResponse.toDomain(): UserBlock = UserBlock(
    blockedBy = blocked_by_user_id,
    userId = blocked_user_id,
    blockedAt = created_at,
)
