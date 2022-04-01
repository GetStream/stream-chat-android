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
 
package io.getstream.chat.android.offline.repository.domain.channel.internal

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.toModel
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.toModel
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.toEntity
import java.util.Date

internal fun Channel.toEntity(): ChannelEntity {
    var lastMessage: MessageEntity? = null
    var lastMessageAt: Date? = null
    messages.lastOrNull()?.let { message ->
        lastMessage = message.toEntity()
        lastMessageAt = message.createdAt
    }
    return ChannelEntity(
        type = type,
        channelId = id,
        name = name,
        image = image,
        cooldown = cooldown,
        frozen = frozen,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        extraData = extraData,
        syncStatus = syncStatus,
        hidden = hidden,
        hideMessagesBefore = hiddenMessagesBefore,
        members = members.map(Member::toEntity).associateBy(MemberEntity::userId).toMutableMap(),
        memberCount = memberCount,
        reads = read.map(ChannelUserRead::toEntity).associateBy(ChannelUserReadEntity::userId).toMutableMap(),
        lastMessageId = lastMessage?.messageInnerEntity?.id,
        lastMessageAt = lastMessageAt,
        createdByUserId = createdBy.id,
        watcherIds = watchers.map(User::id),
        watcherCount = watcherCount,
        team = team,
        ownCapabilities = ownCapabilities,
    )
}

internal suspend fun ChannelEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getMessage: suspend (messageId: String) -> Message?,
): Channel = Channel(
    cooldown = cooldown,
    type = type,
    id = channelId,
    name = name,
    image = image,
    cid = cid,
    frozen = frozen,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    extraData = extraData.toMutableMap(),
    lastMessageAt = lastMessageAt,
    syncStatus = syncStatus,
    hidden = hidden,
    hiddenMessagesBefore = hideMessagesBefore,
    members = members.values.map { it.toModel(getUser) },
    memberCount = memberCount,
    messages = listOfNotNull(lastMessageId?.let { getMessage(it) }),
    read = reads.values.map { it.toModel(getUser) },
    createdBy = getUser(createdByUserId),
    watchers = watcherIds.map { getUser(it) },
    watcherCount = watcherCount,
    team = team,
    ownCapabilities = ownCapabilities,
)
