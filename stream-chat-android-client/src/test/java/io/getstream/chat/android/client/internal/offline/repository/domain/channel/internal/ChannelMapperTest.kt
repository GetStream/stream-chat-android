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

package io.getstream.chat.android.client.internal.offline.repository.domain.channel.internal

import io.getstream.chat.android.client.MockChatClientBuilder
import io.getstream.chat.android.client.internal.offline.randomChannelEntity
import io.getstream.chat.android.client.internal.offline.randomLocationEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.member.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.userread.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.toModel
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ChannelMapperTest {

    @Test
    fun `Should map Channel to ChannelEntity correctly`() = runTest {
        val createdByUser = randomUser()
        val watchers = listOf(randomUser(), randomUser())
        val members = listOf(randomMember(), randomMember())
        val reads = listOf(randomChannelUserRead(), randomChannelUserRead())
        val lastMessage = Message(
            id = randomString(),
            text = randomString(),
            createdAt = randomDate(),
        )
        val membership = randomMember()
        val activeLiveLocations = listOf(
            randomLocation(),
            randomLocation(),
        )

        val channel = randomChannel(
            createdBy = createdByUser,
            watchers = watchers,
            members = members,
            read = reads,
            messages = listOf(lastMessage),
            membership = membership,
            activeLiveLocations = activeLiveLocations.map { it },
        )

        val expectedChannelEntity = ChannelEntity(
            type = channel.type,
            channelId = channel.id,
            name = channel.name,
            image = channel.image,
            cooldown = channel.cooldown,
            filterTags = channel.filterTags,
            frozen = channel.frozen,
            createdAt = channel.createdAt,
            updatedAt = channel.updatedAt,
            deletedAt = channel.deletedAt,
            extraData = channel.extraData,
            syncStatus = channel.syncStatus,
            hidden = channel.hidden,
            hideMessagesBefore = channel.hiddenMessagesBefore,
            members = members.map { it.toEntity() }.associateBy { it.userId }.toMutableMap(),
            memberCount = channel.memberCount,
            reads = reads.map { it.toEntity() }.associateBy { it.userId }.toMutableMap(),
            lastMessageId = lastMessage.id,
            lastMessageAt = channel.lastMessageAt,
            createdByUserId = createdByUser.id,
            watcherIds = watchers.map { it.id },
            watcherCount = channel.watcherCount,
            team = channel.team,
            ownCapabilities = channel.ownCapabilities,
            membership = membership.toEntity(),
            activeLiveLocations = activeLiveLocations.map { it.toEntity() },
            messageCount = channel.messageCount,
        )

        val result = channel.toEntity()

        assertEquals(expectedChannelEntity, result)
    }

    @Test
    @Suppress("LongMethod")
    fun `Should map ChannelEntity to Channel correctly`() = runTest {
        // Instantiate ChatClient so that getCurrentUser() doesn't fail
        MockChatClientBuilder().build()

        val createdByUser = randomUser()
        val watchers = listOf(randomUser(), randomUser())
        val members = listOf(randomMember(), randomMember())
        val reads = listOf(randomChannelUserRead(), randomChannelUserRead())
        val lastMessage = randomMessage()
        val membership = randomMember()
        val draftMessage = randomDraftMessage()
        val activeLiveLocations = listOf(
            randomLocationEntity(),
            randomLocationEntity(),
        )

        val channelEntity = randomChannelEntity(
            createdByUserId = createdByUser.id,
            watcherIds = watchers.map { it.id },
            members = members.map { it.toEntity() }.associateBy { it.userId }.toMutableMap(),
            reads = reads.map { it.toEntity() }.associateBy { it.userId }.toMutableMap(),
            lastMessageId = lastMessage.id,
            membership = membership.toEntity(),
            activeLiveLocations = activeLiveLocations,
        )

        val expectedChannel = Channel(
            cooldown = channelEntity.cooldown,
            type = channelEntity.type,
            id = channelEntity.channelId,
            name = channelEntity.name,
            image = channelEntity.image,
            filterTags = channelEntity.filterTags,
            frozen = channelEntity.frozen,
            createdAt = channelEntity.createdAt,
            updatedAt = channelEntity.updatedAt,
            deletedAt = channelEntity.deletedAt,
            extraData = channelEntity.extraData.toMutableMap(),
            syncStatus = channelEntity.syncStatus,
            hidden = channelEntity.hidden,
            hiddenMessagesBefore = channelEntity.hideMessagesBefore,
            members = members,
            memberCount = channelEntity.memberCount,
            messages = listOf(lastMessage),
            read = reads,
            createdBy = createdByUser,
            watchers = watchers,
            watcherCount = channelEntity.watcherCount,
            team = channelEntity.team,
            ownCapabilities = channelEntity.ownCapabilities,
            membership = membership,
            draftMessage = draftMessage,
            activeLiveLocations = activeLiveLocations.map { it.toModel() },
            messageCount = channelEntity.messageCount,
        )

        val result = channelEntity.toModel(
            getUser = { userId ->
                when (userId) {
                    createdByUser.id -> createdByUser
                    in watchers.map { it.id } -> watchers.first { it.id == userId }
                    in members.map { it.user.id } -> members.first { it.user.id == userId }.user
                    in reads.map { it.user.id } -> reads.first { it.user.id == userId }.user
                    lastMessage.user.id -> lastMessage.user
                    membership.user.id -> membership.user
                    else -> throw IllegalArgumentException("Unknown user id: $userId")
                }
            },
            getMessage = { messageId ->
                if (messageId == lastMessage.id) lastMessage else null
            },
            getDraftMessage = { cid ->
                if (cid == channelEntity.channelId) draftMessage else null
            },
        )
        assertEquals(expectedChannel, result)
    }
}
