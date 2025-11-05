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
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.util.Date

internal class ChannelExtensionsTests {

    @Test
    fun `isAnonymousChannel should return true for anonymous channel`() {
        val anonymousChannel = randomChannel(id = "!members-12345")
        anonymousChannel.isAnonymousChannel() shouldBeEqualTo true
    }

    @Test
    fun `isAnonymousChannel should return false for non-anonymous channel`() {
        val channel = randomChannel(id = "messaging:12345")
        channel.isAnonymousChannel() shouldBeEqualTo false
    }

    @Test
    fun `isPinned should return true if channel is pinned`() {
        val pinnedChannel = randomChannel(membership = randomMember(pinnedAt = randomDate()))
        pinnedChannel.isPinned() shouldBeEqualTo true
    }

    @Test
    fun `isPinned should return false if channel is not pinned`() {
        val channel = randomChannel(membership = randomMember(pinnedAt = null))
        channel.isPinned() shouldBeEqualTo false
    }

    @Test
    fun `isArchive should return true if channel is archived`() {
        val archivedChannel = randomChannel(membership = randomMember(archivedAt = randomDate()))
        archivedChannel.isArchive() shouldBeEqualTo true
    }

    @Test
    fun `isArchive should return false if channel is not archived`() {
        val channel = randomChannel(membership = randomMember(archivedAt = null))
        channel.isArchive() shouldBeEqualTo false
    }

    @Test
    fun `isMutedFor should return true if channel is muted for user`() {
        val channelId = randomCID()
        val channel = randomChannel(id = channelId)
        val mutedUser = randomUser(channelMutes = listOf(randomChannelMute(channel = channel)))
        channel.isMutedFor(mutedUser) shouldBeEqualTo true
    }

    @Test
    fun `isMutedFor should return false if channel is not muted for user`() {
        val channelId = randomCID()
        val channel = randomChannel(id = channelId)
        val user = randomUser()
        channel.isMutedFor(user) shouldBeEqualTo false
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
        users.size shouldBeEqualTo 1
        users.first() shouldBeEqualTo otherUser
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
        channel.countUnreadMentionsForUser(user) shouldBeEqualTo 2
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
        channel.countUnreadMentionsForUser(user) shouldBeEqualTo 1
    }

    @Test
    fun `currentUserUnreadCount should return correct unread count for current user`() {
        val currentUserId = randomString()
        val unreadMessages = positiveRandomInt()
        val channelRead = randomChannelUserRead(user = randomUser(id = currentUserId), unreadMessages = unreadMessages)
        val channel = randomChannel(read = listOf(channelRead))
        channel.currentUserUnreadCount(currentUserId) shouldBeEqualTo unreadMessages
    }

    @Test
    fun `syncUnreadCountWithReads should synchronize unread count with read state`() {
        val currentUserId = randomString()
        val unreadMessages = positiveRandomInt()
        val channelRead = randomChannelUserRead(user = randomUser(id = currentUserId), unreadMessages = unreadMessages)
        val channel = randomChannel(read = listOf(channelRead))
        channel.syncUnreadCountWithReads(currentUserId).unreadCount shouldBeEqualTo unreadMessages
    }
}
