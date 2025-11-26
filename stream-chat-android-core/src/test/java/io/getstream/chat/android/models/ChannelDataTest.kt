/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.models

import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date

internal class ChannelDataTest {

    @Test
    fun `cid should return empty string when id is empty`() {
        // given
        val channelData = ChannelData(
            id = "",
            type = "messaging",
        )
        // when
        val cid = channelData.cid
        // then
        assertEquals("", cid)
    }

    @Test
    fun `cid should return empty string when type is empty`() {
        // given
        val channelData = ChannelData(
            id = "123",
            type = "",
        )
        // when
        val cid = channelData.cid
        // then
        assertEquals("", cid)
    }

    @Test
    fun `cid should return formatted string when both id and type are present`() {
        // given
        val channelData = ChannelData(
            id = "123",
            type = "messaging",
        )
        // when
        val cid = channelData.cid
        // then
        assertEquals("messaging:123", cid)
    }

    @Test
    fun `isUserAbleTo should return true when user has the capability`() {
        // given
        val channelData = ChannelData(
            id = "123",
            type = "messaging",
            ownCapabilities = setOf(
                ChannelCapabilities.SEND_MESSAGE,
                ChannelCapabilities.SEND_REACTION,
                ChannelCapabilities.DELETE_OWN_MESSAGE,
            ),
        )
        // when
        val canSendMessage = channelData.isUserAbleTo(ChannelCapabilities.SEND_MESSAGE)
        // then
        assertTrue(canSendMessage)
    }

    @Test
    fun `isUserAbleTo should return false when user does not have the capability`() {
        // given
        val channelData = ChannelData(
            id = "123",
            type = "messaging",
            ownCapabilities = setOf(
                ChannelCapabilities.SEND_MESSAGE,
                ChannelCapabilities.SEND_REACTION,
            ),
        )
        // when
        val canDeleteChannel = channelData.isUserAbleTo(ChannelCapabilities.DELETE_CHANNEL)
        // then
        assertFalse(canDeleteChannel)
    }

    @Test
    @Suppress("LongMethod")
    fun `toChannel should convert ChannelData to Channel with all properties`() {
        // given
        val createdAt = randomDate()
        val updatedAt = randomDate()
        val deletedAt = randomDate()
        val createdBy = randomUser()
        val membership = randomMember()
        val draft = randomDraftMessage()
        val extraData = mapOf("key1" to "value1", "key2" to 42)
        val filterTags = listOf("tag1", "tag2")

        val channelData = ChannelData(
            id = "123",
            type = "messaging",
            name = "Test Channel",
            image = "https://example.com/image.jpg",
            createdBy = createdBy,
            cooldown = 5,
            frozen = true,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            memberCount = 10,
            team = "team1",
            extraData = extraData,
            ownCapabilities = setOf(ChannelCapabilities.SEND_MESSAGE),
            membership = membership,
            draft = draft,
            messageCount = 100,
            pushPreference = PushPreference(
                level = PushPreferenceLevel.all,
                disabledUntil = null,
            ),
            filterTags = filterTags,
        )

        val messages = listOf(randomMessage())
        val cachedLatestMessages = listOf(randomMessage())
        val members = listOf(randomMember())
        val reads = listOf(randomChannelUserRead())
        val watchers = listOf(randomUser())
        val watcherCount = 5

        // when
        val channel = channelData.toChannel(
            messages = messages,
            cachedLatestMessages = cachedLatestMessages,
            members = members,
            reads = reads,
            watchers = watchers,
            watcherCount = watcherCount,
            insideSearch = true,
        )

        // then
        assertEquals("123", channel.id)
        assertEquals("messaging", channel.type)
        assertEquals("Test Channel", channel.name)
        assertEquals("https://example.com/image.jpg", channel.image)
        assertEquals(filterTags, channel.filterTags)
        assertTrue(channel.frozen)
        assertEquals(createdAt, channel.createdAt)
        assertEquals(updatedAt, channel.updatedAt)
        assertEquals(deletedAt, channel.deletedAt)
        assertEquals(extraData, channel.extraData)
        assertEquals(5, channel.cooldown)
        assertEquals(createdBy, channel.createdBy)
        assertEquals(messages, channel.messages)
        assertEquals(members, channel.members)
        assertEquals(watchers, channel.watchers)
        assertEquals(watcherCount, channel.watcherCount)
        assertEquals(reads, channel.read)
        assertEquals("team1", channel.team)
        assertEquals(10, channel.memberCount)
        assertEquals(setOf(ChannelCapabilities.SEND_MESSAGE), channel.ownCapabilities)
        assertEquals(membership, channel.membership)
        assertEquals(cachedLatestMessages, channel.cachedLatestMessages)
        assertTrue(channel.isInsideSearch)
        assertEquals(draft, channel.draftMessage)
        assertEquals(emptyList<Any>(), channel.activeLiveLocations)
        assertEquals(100, channel.messageCount)
        assertEquals(PushPreferenceLevel.all, channel.pushPreference?.level)
        assertNull(channel.pushPreference?.disabledUntil)
    }

    @Test
    fun `toChannel should handle null optional properties`() {
        // given
        val channelData = ChannelData(
            id = "123",
            type = "messaging",
        )

        // when
        val channel = channelData.toChannel(
            messages = emptyList(),
            cachedLatestMessages = emptyList(),
            members = emptyList(),
            reads = emptyList(),
            watchers = emptyList(),
            watcherCount = 0,
            insideSearch = false,
        )

        // then
        assertEquals("123", channel.id)
        assertEquals("messaging", channel.type)
        assertEquals("", channel.name)
        assertEquals("", channel.image)
        assertEquals(emptyList<String>(), channel.filterTags)
        assertFalse(channel.frozen)
        assertNull(channel.createdAt)
        assertNull(channel.updatedAt)
        assertNull(channel.deletedAt)
        assertEquals(emptyMap<String, Any>(), channel.extraData)
        assertEquals(0, channel.cooldown)
        assertEquals(emptyList<Message>(), channel.messages)
        assertEquals(emptyList<Member>(), channel.members)
        assertEquals(emptyList<User>(), channel.watchers)
        assertEquals(0, channel.watcherCount)
        assertEquals(emptyList<ChannelUserRead>(), channel.read)
        assertEquals("", channel.team)
        assertEquals(0, channel.memberCount)
        assertEquals(emptySet<String>(), channel.ownCapabilities)
        assertNull(channel.membership)
        assertEquals(emptyList<Message>(), channel.cachedLatestMessages)
        assertFalse(channel.isInsideSearch)
        assertNull(channel.draftMessage)
        assertNull(channel.messageCount)
        assertNull(channel.pushPreference)
    }

    @Test
    @Suppress("LongMethod")
    fun `mergeFromEvent should update all mutable fields`() {
        // given
        val originalCreatedAt = Date(1000)
        val originalUpdatedAt = Date(2000)
        val originalCreatedBy = randomUser(id = "user1")

        val original = ChannelData(
            id = "123",
            type = "messaging",
            name = "Old Name",
            image = "old_image.jpg",
            filterTags = listOf("old_tag"),
            frozen = false,
            cooldown = 0,
            team = "old_team",
            extraData = mapOf("old" to "data"),
            memberCount = 5,
            createdAt = originalCreatedAt,
            updatedAt = originalUpdatedAt,
            deletedAt = null,
            createdBy = originalCreatedBy,
            ownCapabilities = setOf(ChannelCapabilities.SEND_MESSAGE),
            membership = randomMember(),
            messageCount = 50,
            pushPreference = PushPreference(level = PushPreferenceLevel.all, disabledUntil = null),
        )

        val newCreatedAt = Date(3000)
        val newUpdatedAt = Date(4000)
        val newDeletedAt = Date(5000)
        val newCreatedBy = randomUser(id = "user2")

        val update = ChannelData(
            id = "123",
            type = "messaging",
            name = "New Name",
            image = "new_image.jpg",
            filterTags = listOf("new_tag1", "new_tag2"),
            frozen = true,
            cooldown = 10,
            team = "new_team",
            extraData = mapOf("new" to "data", "count" to 42),
            memberCount = 15,
            createdAt = newCreatedAt,
            updatedAt = newUpdatedAt,
            deletedAt = newDeletedAt,
            createdBy = newCreatedBy,
            ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL),
            membership = randomMember(),
            messageCount = 100,
            pushPreference = PushPreference(level = PushPreferenceLevel.none, disabledUntil = null),
        )

        // when
        val merged = original.mergeFromEvent(update)

        // then
        assertEquals("New Name", merged.name)
        assertEquals("new_image.jpg", merged.image)
        assertEquals(listOf("new_tag1", "new_tag2"), merged.filterTags)
        assertTrue(merged.frozen)
        assertEquals(10, merged.cooldown)
        assertEquals("new_team", merged.team)
        assertEquals(mapOf("new" to "data", "count" to 42), merged.extraData)
        assertEquals(15, merged.memberCount)
        assertEquals(newCreatedAt, merged.createdAt)
        assertEquals(newUpdatedAt, merged.updatedAt)
        assertEquals(newDeletedAt, merged.deletedAt)
        assertEquals(newCreatedBy, merged.createdBy)
        assertEquals(100, merged.messageCount)
        // These fields should NOT be merged from events
        assertEquals(setOf(ChannelCapabilities.SEND_MESSAGE), merged.ownCapabilities)
        assertEquals(original.membership, merged.membership)
        assertEquals(original.pushPreference, merged.pushPreference)
    }

    @Test
    fun `mergeFromEvent should preserve original messageCount when update has null messageCount`() {
        // given
        val original = ChannelData(
            id = "123",
            type = "messaging",
            messageCount = 50,
        )

        val update = ChannelData(
            id = "123",
            type = "messaging",
            name = "Updated Name",
            messageCount = null,
        )

        // when
        val merged = original.mergeFromEvent(update)

        // then
        assertEquals(50, merged.messageCount)
    }

    @Test
    fun `mergeFromEvent should not merge ownCapabilities field`() {
        // given
        val original = ChannelData(
            id = "123",
            type = "messaging",
            ownCapabilities = setOf(
                ChannelCapabilities.SEND_MESSAGE,
                ChannelCapabilities.SEND_REACTION,
            ),
        )

        val update = ChannelData(
            id = "123",
            type = "messaging",
            name = "Updated Name",
            ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL),
        )

        // when
        val merged = original.mergeFromEvent(update)

        // then
        assertEquals(
            setOf(
                ChannelCapabilities.SEND_MESSAGE,
                ChannelCapabilities.SEND_REACTION,
            ),
            merged.ownCapabilities,
        )
    }

    @Test
    fun `mergeFromEvent should not merge membership field`() {
        // given
        val originalMembership = randomMember(user = randomUser(id = "user1"))
        val original = ChannelData(
            id = "123",
            type = "messaging",
            membership = originalMembership,
        )

        val updateMembership = randomMember(user = randomUser(id = "user2"))
        val update = ChannelData(
            id = "123",
            type = "messaging",
            name = "Updated Name",
            membership = updateMembership,
        )

        // when
        val merged = original.mergeFromEvent(update)

        // then
        assertEquals(originalMembership, merged.membership)
    }

    @Test
    fun `mergeFromEvent should not merge pushPreference field`() {
        // given
        val originalPushPreference = PushPreference(
            level = PushPreferenceLevel.all,
            disabledUntil = null,
        )
        val original = ChannelData(
            id = "123",
            type = "messaging",
            pushPreference = originalPushPreference,
        )

        val updatePushPreference = PushPreference(
            level = PushPreferenceLevel.none,
            disabledUntil = randomDate(),
        )
        val update = ChannelData(
            id = "123",
            type = "messaging",
            name = "Updated Name",
            pushPreference = updatePushPreference,
        )

        // when
        val merged = original.mergeFromEvent(update)

        // then
        assertEquals(originalPushPreference, merged.pushPreference)
    }
}
