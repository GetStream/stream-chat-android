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

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User

internal fun DownstreamChannelDto.toDomain(): Channel =
    Channel(
        id = id,
        type = type,
        name = name ?: "",
        image = image ?: "",
        watcherCount = watcher_count,
        frozen = frozen,
        lastMessageAt = last_message_at,
        createdAt = created_at,
        deletedAt = deleted_at,
        updatedAt = updated_at,
        memberCount = member_count,
        messages = messages.map(DownstreamMessageDto::toDomain),
        members = members.map(DownstreamMemberDto::toDomain),
        watchers = watchers.map(DownstreamUserDto::toDomain),
        read = read.map { it.toDomain(last_message_at ?: it.last_read) },
        config = config.toDomain(),
        createdBy = created_by?.toDomain() ?: User(),
        team = team,
        cooldown = cooldown,
        pinnedMessages = pinned_messages.map(DownstreamMessageDto::toDomain),
        ownCapabilities = own_capabilities.toSet(),
        membership = membership?.toDomain(),
        extraData = extraData.toMutableMap(),
    )
