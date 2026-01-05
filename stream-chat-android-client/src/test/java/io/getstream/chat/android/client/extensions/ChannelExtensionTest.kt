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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.Date

internal class ChannelExtensionTest {

    @Test
    fun `isAnonymousChannel should return true for anonymous channel`() {
        val anonymousChannel = randomChannel(id = "!members-12345")
        assertTrue(anonymousChannel.isAnonymousChannel())
    }

    @Test
    fun `isAnonymousChannel should return false for non-anonymous channel`() {
        val channel = randomChannel(id = "messaging:12345")
        assertFalse(channel.isAnonymousChannel())
    }

    @Test
    fun `isPinned should return true if channel is pinned`() {
        val pinnedChannel = randomChannel(membership = randomMember(pinnedAt = randomDate()))
        assertTrue(pinnedChannel.isPinned())
    }

    @Test
    fun `isPinned should return false if channel is not pinned`() {
        val channel = randomChannel(membership = randomMember(pinnedAt = null))
        assertFalse(channel.isPinned())
    }

    @Test
    fun `isArchive should return true if channel is archived`() {
        val archivedChannel = randomChannel(membership = randomMember(archivedAt = randomDate()))
        assertTrue(archivedChannel.isArchive())
    }

    @Test
    fun `isArchive should return false if channel is not archived`() {
        val channel = randomChannel(membership = randomMember(archivedAt = null))
        assertFalse(channel.isArchive())
    }

    @Test
    fun `isMutedFor should return true if channel is muted for user`() {
        val channelId = randomCID()
        val channel = randomChannel(id = channelId)
        val mutedUser = randomUser(channelMutes = listOf(randomChannelMute(channel = channel)))
        assertTrue(channel.isMutedFor(mutedUser))
    }

    @Test
    fun `isMutedFor should return false if channel is not muted for user`() {
        val channelId = randomCID()
        val channel = randomChannel(id = channelId)
        val user = randomUser()
        assertFalse(channel.isMutedFor(user))
    }

    @Test
    fun `getUsersExcludingCurrent should return users excluding current user`() {
        val currentUser = randomUser()
        val otherUser = randomUser()
        val members = listOf(
            randomMember(user = currentUser),
            randomMember(user = otherUser),
        )
        val channel = randomChannel(members = members)
        val users = channel.getUsersExcludingCurrent(currentUser)
        assertEquals(1, users.size)
        assertEquals(otherUser, users.first())
    }

    @Test
    fun `countUnreadMentionsForUser should return correct count when lastMessageSeenDate is null`() {
        val user = randomUser()
        val messages = listOf(
            randomMessage(
                text = "Hello @${user.id}",
                mentionedUsers = listOf(user),
            ),
            randomMessage(
                text = "Hi @${user.id}",
                mentionedUsers = listOf(user),
            ),
            randomMessage(
                text = "No mention here",
            ),
        )
        val channel = randomChannel(messages = messages)
        assertEquals(2, channel.countUnreadMentionsForUser(user))
    }

    @Test
    fun `countUnreadMentionsForUser should return correct count when lastMessageSeenDate is not null`() {
        val user = randomUser()
        val lastReadDate = Date()
        val messages = listOf(
            randomMessage(
                text = "Hello @${user.id}",
                mentionedUsers = listOf(user),
                createdAt = Date(lastReadDate.time - 1000),
                createdLocallyAt = null,
            ),
            randomMessage(
                text = "Hi @${user.id}",
                mentionedUsers = listOf(user),
                createdAt = Date(lastReadDate.time + 1000),
                createdLocallyAt = null,
            ),
            randomMessage(
                text = "No mention here",
                createdAt = Date(lastReadDate.time + 2000),
                createdLocallyAt = null,
            ),
        )
        val channelRead = randomChannelUserRead(user = user, lastRead = lastReadDate)
        val channel = randomChannel(messages = messages, read = listOf(channelRead))
        assertEquals(1, channel.countUnreadMentionsForUser(user))
    }

    @Test
    fun `currentUserUnreadCount should return correct unread count for current user`() {
        val currentUserId = randomString()
        val unreadMessages = positiveRandomInt()
        val channelRead = randomChannelUserRead(user = randomUser(id = currentUserId), unreadMessages = unreadMessages)
        val channel = randomChannel(read = listOf(channelRead))
        assertEquals(unreadMessages, channel.currentUserUnreadCount(currentUserId))
    }

    @Test
    fun `syncUnreadCountWithReads should synchronize unread count with read state`() {
        val currentUserId = randomString()
        val unreadMessages = positiveRandomInt()
        val channelRead = randomChannelUserRead(user = randomUser(id = currentUserId), unreadMessages = unreadMessages)
        val channel = randomChannel(read = listOf(channelRead))
        assertEquals(unreadMessages, channel.syncUnreadCountWithReads(currentUserId).unreadCount)
    }

    @Test
    fun `readsOf should return correct list of ChannelUserRead who have read the message`() {
        val createdAt = randomDate()
        val messageUser = randomUser()
        val otherUser1 = randomUser()
        val otherUser2 = randomUser()
        val lastRead = Date(createdAt.time + 1000) // After message creation time
        val read1 = randomChannelUserRead(user = otherUser1, lastRead = lastRead)
        val read2 = randomChannelUserRead(user = otherUser2, lastRead = lastRead)
        val read3 = randomChannelUserRead(user = messageUser, lastRead = lastRead)
        val channel = randomChannel(read = listOf(read1, read2, read3))
        val message = randomMessage(user = messageUser, createdLocallyAt = createdAt)

        val actual = channel.readsOf(message)

        assertEquals(2, actual.size)
        assertEquals(listOf(read1, read2), actual)
    }

    @Test
    fun `deliveredReadsOf should return correct list of ChannelUserRead who have delivered the message`() {
        val createdAt = randomDate()
        val messageUser = randomUser()
        val otherUser1 = randomUser()
        val otherUser2 = randomUser()
        val lastDelivered = Date(createdAt.time + 1000) // After message creation time
        val delivered1 = randomChannelUserRead(user = otherUser1, lastDeliveredAt = lastDelivered)
        val delivered2 = randomChannelUserRead(user = otherUser2, lastDeliveredAt = lastDelivered)
        val delivered3 = randomChannelUserRead(user = messageUser, lastDeliveredAt = lastDelivered)
        val channel = randomChannel(read = listOf(delivered1, delivered2, delivered3))
        val message = randomMessage(user = messageUser, createdLocallyAt = createdAt)

        val actual = channel.deliveredReadsOf(message)

        assertEquals(2, actual.size)
        assertEquals(listOf(delivered1, delivered2), actual)
    }
}
