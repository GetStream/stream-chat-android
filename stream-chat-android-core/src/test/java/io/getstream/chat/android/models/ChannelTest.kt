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

package io.getstream.chat.android.models

import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPendingMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomSyncStatus
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date

internal class ChannelTest {

    @Test
    @Suppress("DEPRECATION", "LongMethod")
    fun `builder should set every field`() {
        val expected = Channel(
            id = randomString(),
            type = randomString(),
            name = randomString(),
            image = randomString(),
            watcherCount = randomInt(),
            frozen = randomBoolean(),
            createdAt = randomDate(),
            deletedAt = randomDate(),
            updatedAt = randomDate(),
            syncStatus = randomSyncStatus(),
            memberCount = randomInt(),
            messages = listOf(randomMessage()),
            members = listOf(randomMember()),
            watchers = listOf(randomUser()),
            read = listOf(randomChannelUserRead()),
            config = randomConfig(),
            createdBy = randomUser(),
            unreadCount = randomInt(),
            team = randomString(),
            hidden = randomBoolean(),
            hiddenMessagesBefore = randomDate(),
            cooldown = randomInt(),
            pinnedMessages = listOf(randomMessage()),
            ownCapabilities = setOf(randomString()),
            membership = randomMember(),
            cachedLatestMessages = listOf(randomMessage()),
            isInsideSearch = randomBoolean(),
            draftMessage = randomDraftMessage(),
            activeLiveLocations = listOf(randomLocation()),
            messageCount = randomInt(),
            pushPreference = PushPreference(level = PushPreferenceLevel.all, disabledUntil = randomDate()),
            filterTags = listOf(randomString()),
            lastMessageAt = randomDate(),
            extraData = mapOf(randomString() to randomString()),
        )

        val built = Channel.Builder()
            .withId(expected.id)
            .withType(expected.type)
            .withName(expected.name)
            .withImage(expected.image)
            .withWatcherCount(expected.watcherCount)
            .withFrozen(expected.frozen)
            .withCreatedAt(expected.createdAt)
            .withDeletedAt(expected.deletedAt)
            .withUpdatedAt(expected.updatedAt)
            .withSyncStatus(expected.syncStatus)
            .withMemberCount(expected.memberCount)
            .withMessages(expected.messages)
            .withMembers(expected.members)
            .withWatchers(expected.watchers)
            .withRead(expected.read)
            .withConfig(expected.config)
            .withCreatedBy(expected.createdBy)
            .withUnreadCount(expected.unreadCount)
            .withTeam(expected.team)
            .withHidden(expected.hidden)
            .withHiddenMessagesBefore(expected.hiddenMessagesBefore)
            .withCooldown(expected.cooldown)
            .withPinnedMessages(expected.pinnedMessages)
            .withOwnCapabilities(expected.ownCapabilities)
            .withMembership(expected.membership)
            .withCachedLatestMessages(expected.cachedLatestMessages)
            .withIsInsideSearch(expected.isInsideSearch)
            .withDraftMessage(expected.draftMessage)
            .withActiveLiveLocations(expected.activeLiveLocations)
            .withMessageCount(expected.messageCount)
            .withPushPreference(expected.pushPreference)
            .withFilterTags(expected.filterTags)
            .withLastMessageAt(expected.lastMessageAt)
            .withExtraData(expected.extraData)
            .build()

        assertEquals(expected, built)
    }

    @Test
    fun `builder copy constructor should copy every field`() {
        val channel = randomChannel(pendingMessages = emptyList())

        val built = Channel.Builder(channel).build()

        assertEquals(channel, built)
    }

    @Test
    fun `builder copy constructor should not carry pendingMessages`() {
        val channel = randomChannel(pendingMessages = listOf(randomPendingMessage()))

        val built = Channel.Builder(channel).build()

        assertEquals(emptyList<PendingMessage>(), built.pendingMessages)
        assertEquals(channel.copy(pendingMessages = emptyList()), built)
    }

    @Test
    fun `cid should return empty string when id is empty`() {
        val channel = randomChannel(id = "")
        assertEquals("", channel.cid)
    }

    @Test
    fun `cid should return empty string when type is empty`() {
        val channel = randomChannel(type = "")
        assertEquals("", channel.cid)
    }

    @Test
    fun `cid should return formatted string when both id and type are present`() {
        val channel = randomChannel(id = "123", type = "messaging")
        assertEquals("messaging:123", channel.cid)
    }

    @Test
    fun `lastUpdated should return lastMessageAt when it is after createdAt`() {
        val channel = randomChannel(createdAt = Date(1000), lastMessageAt = Date(2000))
        assertEquals(Date(2000), channel.lastUpdated)
    }

    @Test
    fun `lastUpdated should return createdAt when lastMessageAt is before createdAt`() {
        val channel = randomChannel(createdAt = Date(2000), lastMessageAt = Date(1000))
        assertEquals(Date(2000), channel.lastUpdated)
    }

    @Test
    fun `lastUpdated should return createdAt when lastMessageAt is null`() {
        val channel = randomChannel(createdAt = Date(1000), lastMessageAt = null)
        assertEquals(Date(1000), channel.lastUpdated)
    }

    @Test
    fun `lastUpdated should return lastMessageAt when createdAt is null`() {
        val channel = randomChannel(createdAt = null, lastMessageAt = Date(2000))
        assertEquals(Date(2000), channel.lastUpdated)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `hasUnread should reflect the unread count`() {
        assertTrue(randomChannel(unreadCount = 1).hasUnread)
        assertFalse(randomChannel(unreadCount = 0).hasUnread)
    }

    @Test
    fun `getComparableField should return string fields`() {
        val channel = randomChannel()
        assertEquals(channel.cid, channel.getComparableField("cid"))
        assertEquals(channel.id, channel.getComparableField("id"))
        assertEquals(channel.type, channel.getComparableField("type"))
        assertEquals(channel.name, channel.getComparableField("name"))
        assertEquals(channel.image, channel.getComparableField("image"))
        assertEquals(channel.team, channel.getComparableField("team"))
    }

    @Test
    fun `getComparableField should return count fields for snake_case and camelCase field names`() {
        val channel = randomChannel(watcherCount = 3, memberCount = 5, unreadCount = 7)
            .copy(cooldown = 11)
        assertEquals(3, channel.getComparableField("watcher_count"))
        assertEquals(3, channel.getComparableField("watcherCount"))
        assertEquals(5, channel.getComparableField("member_count"))
        assertEquals(5, channel.getComparableField("memberCount"))
        assertEquals(7, channel.getComparableField("unread_count"))
        assertEquals(7, channel.getComparableField("unreadCount"))
        assertEquals(true, channel.getComparableField("has_unread"))
        assertEquals(true, channel.getComparableField("hasUnread"))
        assertEquals(11, channel.getComparableField("cooldown"))
    }

    @Test
    fun `getComparableField should return boolean fields`() {
        val channel = randomChannel(frozen = true, hidden = false)
        assertEquals(true, channel.getComparableField("frozen"))
        assertEquals(false, channel.getComparableField("hidden"))
    }

    @Test
    fun `getComparableField should return date fields for snake_case and camelCase field names`() {
        val channel = randomChannel(lastMessageAt = randomDate())
        assertEquals(channel.lastMessageAt, channel.getComparableField("last_message_at"))
        assertEquals(channel.lastMessageAt, channel.getComparableField("lastMessageAt"))
        assertEquals(channel.createdAt, channel.getComparableField("created_at"))
        assertEquals(channel.createdAt, channel.getComparableField("createdAt"))
        assertEquals(channel.updatedAt, channel.getComparableField("updated_at"))
        assertEquals(channel.updatedAt, channel.getComparableField("updatedAt"))
        assertEquals(channel.deletedAt, channel.getComparableField("deleted_at"))
        assertEquals(channel.deletedAt, channel.getComparableField("deletedAt"))
        assertEquals(channel.lastUpdated, channel.getComparableField("last_updated"))
        assertEquals(channel.lastUpdated, channel.getComparableField("lastUpdated"))
    }

    @Test
    fun `getComparableField should return membership dates for pinned_at and archived_at`() {
        val pinnedAt = randomDate()
        val archivedAt = randomDate()
        val channel = randomChannel(membership = randomMember(pinnedAt = pinnedAt, archivedAt = archivedAt))
        assertEquals(pinnedAt, channel.getComparableField("pinned_at"))
        assertEquals(pinnedAt, channel.getComparableField("pinnedAt"))
        assertEquals(archivedAt, channel.getComparableField("archived_at"))
        assertEquals(archivedAt, channel.getComparableField("archivedAt"))
    }

    @Test
    fun `getComparableField should return extraData value for custom field`() {
        val channel = randomChannel(extraData = mapOf("customField" to "customValue"))
        assertEquals("customValue", channel.getComparableField("customField"))
    }

    @Test
    fun `getComparableField should return null for unknown field`() {
        val channel = randomChannel(extraData = emptyMap())
        assertNull(channel.getComparableField("unknownField"))
    }

    @Test
    fun `mergeChannelFromEvent should update channel data fields`() {
        val original = randomChannel(
            hidden = false,
            frozen = false,
            syncStatus = SyncStatus.COMPLETED,
            messageCount = null,
            extraData = mapOf("key" to "original"),
        )
        val update = randomChannel(
            hidden = true,
            frozen = true,
            syncStatus = SyncStatus.SYNC_NEEDED,
            messageCount = positiveRandomInt(),
            members = listOf(randomMember()),
            config = randomConfig(),
            extraData = mapOf("key" to "updated"),
        )

        val merged = original.mergeChannelFromEvent(update)

        assertEquals(update.name, merged.name)
        assertEquals(update.image, merged.image)
        assertEquals(update.hidden, merged.hidden)
        assertEquals(update.frozen, merged.frozen)
        assertEquals(update.filterTags, merged.filterTags)
        assertEquals(update.team, merged.team)
        assertEquals(update.config, merged.config)
        assertEquals(update.extraData, merged.extraData)
        assertEquals(update.syncStatus, merged.syncStatus)
        assertEquals(update.hiddenMessagesBefore, merged.hiddenMessagesBefore)
        assertEquals(update.memberCount, merged.memberCount)
        assertEquals(update.members, merged.members)
        assertEquals(update.createdAt, merged.createdAt)
        assertEquals(update.updatedAt, merged.updatedAt)
        assertEquals(update.deletedAt, merged.deletedAt)
        assertEquals(update.messageCount, merged.messageCount)
        assertEquals(update.lastMessageAt, merged.lastMessageAt)
    }

    @Test
    fun `mergeChannelFromEvent should keep messageCount when the update has none`() {
        val original = randomChannel(messageCount = 50)
        val update = randomChannel(messageCount = null)

        val merged = original.mergeChannelFromEvent(update)

        assertEquals(50, merged.messageCount)
    }

    @Test
    fun `mergeChannelFromEvent should always take disabled from the update`() {
        val original = randomChannel(disabled = true)
        val update = randomChannel(disabled = false)

        val merged = original.mergeChannelFromEvent(update)

        assertFalse(merged.disabled)
    }

    @Test
    fun `mergeChannelFromEvent should keep blocked when the update has none`() {
        val original = randomChannel(blocked = true)
        val update = randomChannel(blocked = null)

        val merged = original.mergeChannelFromEvent(update)

        assertTrue(merged.blocked!!)
    }

    @Test
    fun `mergeChannelFromEvent should keep the latest truncatedAt`() {
        val earlier = Date(1000)
        val later = Date(2000)

        fun merge(original: Date?, update: Date?): Date? =
            randomChannel(truncatedAt = original).mergeChannelFromEvent(randomChannel(truncatedAt = update)).truncatedAt

        assertEquals(later, merge(original = later, update = earlier))
        assertEquals(later, merge(original = earlier, update = later))
        assertEquals(later, merge(original = later, update = null))
        assertEquals(later, merge(original = null, update = later))
        assertNull(merge(original = null, update = null))
    }

    @Test
    @Suppress("DEPRECATION")
    fun `mergeChannelFromEvent should not merge connection specific fields`() {
        val original = randomChannel(
            messages = listOf(randomMessage()),
            watcherCount = 1,
            watchers = listOf(randomUser()),
            read = listOf(randomChannelUserRead()),
            ownCapabilities = setOf("send-message"),
            membership = randomMember(),
            unreadCount = 5,
        )
        val update = randomChannel(
            messages = listOf(randomMessage()),
            watcherCount = 2,
            watchers = listOf(randomUser()),
            read = listOf(randomChannelUserRead()),
            ownCapabilities = setOf("delete-channel"),
            membership = randomMember(),
            unreadCount = 10,
        )

        val merged = original.mergeChannelFromEvent(update)

        assertEquals(original.messages, merged.messages)
        assertEquals(original.watcherCount, merged.watcherCount)
        assertEquals(original.watchers, merged.watchers)
        assertEquals(original.read, merged.read)
        assertEquals(original.ownCapabilities, merged.ownCapabilities)
        assertEquals(original.membership, merged.membership)
        assertEquals(original.unreadCount, merged.unreadCount)
    }

    @Test
    fun `toChannelData should map all channel data fields`() {
        val channel = randomChannel(
            draftMessage = randomDraftMessage(),
            messageCount = positiveRandomInt(),
            lastMessageAt = randomDate(),
        ).copy(
            pushPreference = PushPreference(level = PushPreferenceLevel.all, disabledUntil = randomDate()),
        )

        val channelData = channel.toChannelData()

        assertEquals(channel.type, channelData.type)
        assertEquals(channel.id, channelData.id)
        assertEquals(channel.name, channelData.name)
        assertEquals(channel.image, channelData.image)
        assertEquals(channel.filterTags, channelData.filterTags)
        assertEquals(channel.frozen, channelData.frozen)
        assertEquals(channel.cooldown, channelData.cooldown)
        assertEquals(channel.createdAt, channelData.createdAt)
        assertEquals(channel.updatedAt, channelData.updatedAt)
        assertEquals(channel.deletedAt, channelData.deletedAt)
        assertEquals(channel.memberCount, channelData.memberCount)
        assertEquals(channel.extraData, channelData.extraData)
        assertEquals(channel.createdBy, channelData.createdBy)
        assertEquals(channel.team, channelData.team)
        assertEquals(channel.ownCapabilities, channelData.ownCapabilities)
        assertEquals(channel.membership, channelData.membership)
        assertEquals(channel.draftMessage, channelData.draft)
        assertEquals(channel.messageCount, channelData.messageCount)
        assertEquals(channel.pushPreference, channelData.pushPreference)
        assertEquals(channel.lastMessageAt, channelData.lastMessageAt)
        assertEquals(channel.truncatedAt, channelData.truncatedAt)
        assertEquals(channel.disabled, channelData.disabled)
        assertEquals(channel.blocked, channelData.blocked)
    }
}
