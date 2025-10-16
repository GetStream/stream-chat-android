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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMembers
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldNotContain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChannelExtensionsTests {

    @Test
    fun `users() should return all users associated with the channel`() {
        // given
        val members = randomMembers()
        val createdBy = randomUser()
        val messages = listOf(randomMessage(), randomMessage())
        val watchers = listOf(randomUser())
        val channel = randomChannel(
            members = members,
            createdBy = createdBy,
            messages = messages,
            watchers = watchers,
        )
        // when
        val users = channel.users()
        // then
        val expectedUsers = members.map { it.user } + createdBy + messages.flatMap { it.users() } + watchers
        users shouldBeEqualTo expectedUsers
    }

    @Test
    fun `lastMessage should return the most recent non-deleted message`() {
        // given
        val oldestMsg = randomMessage(createdAt = Date(1000), deletedAt = null, deletedForMe = false)
        val newestMsg = randomMessage(createdAt = Date(3000), deletedAt = null, deletedForMe = false)
        val middleMsg = randomMessage(
            createdAt = null,
            createdLocallyAt = Date(2000),
            deletedAt = null,
            deletedForMe = false,
        )
        val deletedMsg = randomMessage(createdAt = Date(4000), deletedAt = Date(5000))

        val channel = randomChannel(
            messages = listOf(oldestMsg, newestMsg, middleMsg, deletedMsg),
        )

        // when
        val result = channel.lastMessage

        // then
        result shouldBeEqualTo newestMsg
    }

    @Test
    fun `updateLastMessage should add message to channel messages and update reads`() {
        // given
        val currentUserId = "current-user"
        val currentUserRead = ChannelUserRead(
            user = randomUser(id = currentUserId),
            lastRead = Date(1000),
            lastReceivedEventDate = Date(1000),
            unreadMessages = 0,
            lastReadMessageId = null,
        )
        val otherUserRead = ChannelUserRead(
            user = randomUser(id = "other-user"),
            lastRead = Date(1000),
            lastReceivedEventDate = Date(1000),
            unreadMessages = 0,
            lastReadMessageId = null,
        )
        val existingMessage = randomMessage(
            id = "existing",
            createdAt = Date(1000),
            deletedAt = null,
            deletedForMe = false,
        )
        val newMessage = randomMessage(
            id = "new",
            createdAt = Date(2000),
            deletedAt = null,
            deletedForMe = false,
            silent = false,
        )
        val receivedEventDate = Date(2500)

        val channel = randomChannel(
            messages = listOf(existingMessage),
            read = listOf(currentUserRead, otherUserRead),
        )

        // when
        val result = channel.updateLastMessage(receivedEventDate, newMessage, currentUserId)

        // then
        result.messages shouldContain existingMessage
        result.messages shouldContain newMessage

        // Check that current user read was updated
        val updatedCurrentUserRead = result.read.find { it.user.id == currentUserId }
        updatedCurrentUserRead?.lastReceivedEventDate shouldBeEqualTo receivedEventDate
        updatedCurrentUserRead?.unreadMessages shouldBeEqualTo 1 // Incremented for current user

        // Check that other user's read was not changed
        val updatedOtherUserRead = result.read.find { it.user.id == "other-user" }
        updatedOtherUserRead?.lastReceivedEventDate shouldBeEqualTo Date(1000)
        updatedOtherUserRead?.unreadMessages shouldBeEqualTo 0
    }

    @Test
    fun `updateLastMessage with a silent message should add message to channel messages but not update reads`() {
        // given
        val currentUserId = "current-user"
        val currentUserRead = ChannelUserRead(
            user = randomUser(id = currentUserId),
            lastRead = Date(1000),
            lastReceivedEventDate = Date(1000),
            unreadMessages = 0,
            lastReadMessageId = null,
        )
        val otherUserRead = ChannelUserRead(
            user = randomUser(id = "other-user"),
            lastRead = Date(1000),
            lastReceivedEventDate = Date(1000),
            unreadMessages = 0,
            lastReadMessageId = null,
        )
        val existingMessage = randomMessage(
            id = "existing",
            createdAt = Date(1000),
            deletedAt = null,
            deletedForMe = false,
        )
        val newMessage = randomMessage(
            id = "new",
            createdAt = Date(2000),
            deletedAt = null,
            deletedForMe = false,
            silent = true,
        )
        val receivedEventDate = Date(2500)

        val channel = randomChannel(
            messages = listOf(existingMessage),
            read = listOf(currentUserRead, otherUserRead),
        )

        // when
        val result = channel.updateLastMessage(receivedEventDate, newMessage, currentUserId)

        // then
        result.messages shouldContain existingMessage
        result.messages shouldContain newMessage

        // Check that current user read was updated
        val updatedCurrentUserRead = result.read.find { it.user.id == currentUserId }
        updatedCurrentUserRead?.lastReceivedEventDate shouldBeEqualTo receivedEventDate
        updatedCurrentUserRead?.unreadMessages shouldBeEqualTo 0

        // Check that other user's read was not changed
        val updatedOtherUserRead = result.read.find { it.user.id == "other-user" }
        updatedOtherUserRead?.lastReceivedEventDate shouldBeEqualTo Date(1000)
        updatedOtherUserRead?.unreadMessages shouldBeEqualTo 0
    }

    @Test
    fun `removeMember should remove member from channel by user id`() {
        // given
        val memberToRemove = randomMember(user = randomUser(id = "user-to-remove"))
        val memberToKeep = randomMember(user = randomUser(id = "user-to-keep"))
        val channel = randomChannel(
            members = listOf(memberToRemove, memberToKeep),
            memberCount = 2,
        )

        // when
        val result = channel.removeMember("user-to-remove")

        // then
        result.memberCount shouldBeEqualTo 1
        result.members shouldContain memberToKeep
        result.members shouldNotContain memberToRemove
    }

    @Test
    fun `removeMember should not change channel if member not found`() {
        // given
        val member1 = randomMember(user = randomUser(id = "user-1"))
        val member2 = randomMember(user = randomUser(id = "user-2"))
        val channel = randomChannel(
            members = listOf(member1, member2),
            memberCount = 2,
        )

        // when
        val result = channel.removeMember("user-3")

        // then
        result.memberCount shouldBeEqualTo 2
        result.members shouldContain member1
        result.members shouldContain member2
    }

    @Test
    fun `addMember should add a new member to the channel`() {
        // given
        val existingMember = randomMember(user = randomUser(id = "existing"))
        val newMember = randomMember(user = randomUser(id = "new"))
        val channel = randomChannel(
            members = listOf(existingMember),
            memberCount = 1,
        )

        // when
        val result = channel.addMember(newMember)

        // then
        result.memberCount shouldBeEqualTo 2
        result.members shouldContainAll listOf(existingMember, newMember)
    }

    @Test
    fun `addMember should not duplicate members with same user id`() {
        // given
        val existingMember = randomMember(user = randomUser(id = "existing"))
        val newMemberSameId = randomMember(user = randomUser(id = "existing"))
        val channel = randomChannel(
            members = listOf(existingMember),
            memberCount = 1,
        )

        // when
        val result = channel.addMember(newMemberSameId)

        // then
        result.memberCount shouldBeEqualTo 1 // Count unchanged
        result.members.size shouldBeEqualTo 1 // Still only one member
    }

    @Test
    fun `updateMember should replace member with same user id`() {
        // given
        val originalMember = randomMember(user = randomUser(id = "target"))
        val otherMember = randomMember(user = randomUser(id = "other"))
        val updatedMember = randomMember(user = randomUser(id = "target"))

        val channel = randomChannel(
            members = listOf(originalMember, otherMember),
        )

        // when
        val result = channel.updateMember(updatedMember)

        // then
        result.members shouldContain updatedMember
        result.members shouldContain otherMember
        result.members shouldNotContain originalMember
    }

    @Test
    fun `updateMemberBanned should update banned status for specific member`() {
        // given
        val targetMember = randomMember(user = randomUser(id = "target"), banned = false, shadowBanned = false)
        val otherMember = randomMember(user = randomUser(id = "other"), banned = false, shadowBanned = false)

        val channel = randomChannel(
            members = listOf(targetMember, otherMember),
        )

        // when
        val result = channel.updateMemberBanned("target", true, true)

        // then
        val updatedTargetMember = result.members.find { it.user.id == "target" }
        updatedTargetMember?.banned shouldBeEqualTo true
        updatedTargetMember?.shadowBanned shouldBeEqualTo true

        val unchangedOtherMember = result.members.find { it.user.id == "other" }
        unchangedOtherMember?.banned shouldBeEqualTo false
        unchangedOtherMember?.shadowBanned shouldBeEqualTo false
    }

    @Test
    fun `addMembership should update channel membership if current user is the member`() {
        // given
        val currentUserId = "current-user"
        val member = randomMember(user = randomUser(id = currentUserId))
        val channel = randomChannel(membership = null)

        // when
        val result = channel.addMembership(currentUserId, member)

        // then
        result.membership shouldBeEqualTo member
    }

    @Test
    fun `addMembership should not update channel membership if different user`() {
        // given
        val currentUserId = "current-user"
        val differentUserId = "different-user"
        val member = randomMember(user = randomUser(id = differentUserId))
        val channel = randomChannel(membership = null)

        // when
        val result = channel.addMembership(currentUserId, member)

        // then
        result.membership shouldBeEqualTo null
    }

    @Test
    fun `updateMembership should update membership when member has same user id`() {
        // given
        val existingMembership = randomMember(user = randomUser(id = "user"))
        val updatedMember = randomMember(user = randomUser(id = "user"))

        val channel = randomChannel(membership = existingMembership)

        // when
        val result = channel.updateMembership(updatedMember)

        // then
        result.membership shouldBeEqualTo updatedMember
    }

    @Test
    fun `updateMembership should not update membership when member has different user id`() {
        // given
        val existingMembership = randomMember(user = randomUser(id = "user-1"))
        val differentMember = randomMember(user = randomUser(id = "user-2"))

        val channel = randomChannel(membership = existingMembership)

        // when
        val result = channel.updateMembership(differentMember)

        // then
        result.membership shouldBeEqualTo existingMembership
    }

    @Test
    fun `updateMembershipBanned should update banned status for membership with matching user id`() {
        // given
        val memberUserId = "user"
        val membership = randomMember(user = randomUser(id = memberUserId), banned = false)

        val channel = randomChannel(membership = membership)

        // when
        val result = channel.updateMembershipBanned(memberUserId, true)

        // then
        result.membership?.banned shouldBeEqualTo true
    }

    @Test
    fun `updateMembershipBanned should not update banned status for different user id`() {
        // given
        val memberUserId = "user"
        val differentUserId = "different-user"
        val membership = randomMember(user = randomUser(id = memberUserId), banned = false)

        val channel = randomChannel(membership = membership)

        // when
        val result = channel.updateMembershipBanned(differentUserId, true)

        // then
        result.membership?.banned shouldBeEqualTo false
    }

    @Test
    fun `removeMembership should remove membership when user id matches`() {
        // given
        val userId = "user"
        val membership = randomMember(user = randomUser(id = userId))

        val channel = randomChannel(membership = membership)

        // when
        val result = channel.removeMembership(userId)

        // then
        result.membership shouldBeEqualTo null
    }

    @Test
    fun `removeMembership should not remove membership when user id differs`() {
        // given
        val userId = "user"
        val differentUserId = "different-user"
        val membership = randomMember(user = randomUser(id = userId))

        val channel = randomChannel(membership = membership)

        // when
        val result = channel.removeMembership(differentUserId)

        // then
        result.membership shouldBeEqualTo membership
    }

    @Test
    fun `updateReads should replace existing read with new read for same user`() {
        // given
        val userId = "user"
        val currentUserId = "current-user" // For syncUnreadCount
        val existingRead = ChannelUserRead(
            user = randomUser(id = userId),
            lastRead = Date(1000),
            lastReceivedEventDate = Date(2000),
            unreadMessages = 5,
            lastReadMessageId = "message-id",
        )
        val otherRead = ChannelUserRead(
            user = randomUser(id = "other"),
            lastRead = Date(1000),
            lastReceivedEventDate = Date(2000),
            unreadMessages = 3,
            lastReadMessageId = "message-id",
        )
        val newRead = ChannelUserRead(
            user = randomUser(id = userId),
            lastRead = Date(2000),
            lastReceivedEventDate = Date(3000),
            unreadMessages = 0,
            lastReadMessageId = "new-message-id",
        )

        val channel = randomChannel(read = listOf(existingRead, otherRead))

        // when
        val result = channel.updateReads(newRead, currentUserId)

        // then
        result.read shouldContain newRead
        result.read shouldContain otherRead
        result.read shouldNotContain existingRead
    }

    @Test
    fun `updateReads should add new read if not exists for user`() {
        // given
        val userId = "new-user"
        val currentUserId = "current-user" // For syncUnreadCount
        val existingRead = ChannelUserRead(
            user = randomUser(id = "existing"),
            lastRead = Date(1000),
            lastReceivedEventDate = Date(2000),
            unreadMessages = 5,
            lastReadMessageId = "message-id",
        )

        val newRead = ChannelUserRead(
            user = randomUser(id = userId),
            lastRead = Date(2000),
            lastReceivedEventDate = Date(3000),
            unreadMessages = 0,
            lastReadMessageId = "new-message-id",
        )

        val channel = randomChannel(read = listOf(existingRead))

        // when
        val result = channel.updateReads(newRead, currentUserId)

        // then
        result.read shouldContain existingRead
        result.read shouldContain newRead
        result.read.size shouldBeEqualTo 2
    }

    @Test
    fun `applyPagination should sort and paginate channels properly`() {
        // given
        val channels = listOf(
            randomChannel(id = "1", createdAt = Date(3000)), // Newest
            randomChannel(id = "2", createdAt = Date(1000)), // Oldest
            randomChannel(id = "3", createdAt = Date(2000)), // Middle
        )

        val sort = QuerySortByField<Channel>().desc("created_at")
        val pagination = mock<AnyChannelPaginationRequest>()
        whenever(pagination.sort).thenReturn(sort)
        whenever(pagination.channelOffset).thenReturn(0)
        whenever(pagination.channelLimit).thenReturn(2)

        // when
        val result = channels.applyPagination(pagination)

        // then
        result.size shouldBeEqualTo 2
        result[0].id shouldBeEqualTo "1" // Latest first due to descending sort
        result[1].id shouldBeEqualTo "3" // Second latest
    }

    @Test
    fun `collection updateUsers should update all channels in collection`() {
        // given
        val user1 = randomUser(id = "user1", name = "Old Name 1")
        val user2 = randomUser(id = "user2", name = "Old Name 2")

        val updatedUser1 = randomUser(id = "user1", name = "New Name 1")
        val updatedUser2 = randomUser(id = "user2", name = "New Name 2")

        // Create first channel with user1 as creator and in a message
        val channel1 = randomChannel(
            id = "channel1",
            createdBy = user1,
            messages = listOf(randomMessage(user = user1)),
        )

        // Create second channel with user2 as watcher
        val channel2 = randomChannel(
            id = "channel2",
            watchers = listOf(user2),
        )

        // Create a channel without any users to update
        val channel3 = randomChannel(
            id = "channel3",
            createdBy = randomUser(id = "user3"),
        )

        val channels = listOf(channel1, channel2, channel3)

        val updatedUsers = mapOf(
            "user1" to updatedUser1,
            "user2" to updatedUser2,
        )

        // when
        val updatedChannels = channels.updateUsers(updatedUsers)

        // then
        // First channel: creator should be updated
        updatedChannels[0].createdBy.id shouldBeEqualTo "user1"
        updatedChannels[0].createdBy.name shouldBeEqualTo "New Name 1"
        updatedChannels[0].messages.first().user.name shouldBeEqualTo "New Name 1"

        // Second channel: watcher should be updated
        updatedChannels[1].watchers.first().id shouldBeEqualTo "user2"
        updatedChannels[1].watchers.first().name shouldBeEqualTo "New Name 2"

        // Third channel: should remain unchanged since it has no users to update
        updatedChannels[2].createdBy.id shouldBeEqualTo "user3"
        updatedChannels[2] shouldBeEqualTo channel3
    }

    @Test
    fun `updateUsers should update users in all related collections`() {
        // given
        val user1 = randomUser(id = "user1", name = "Old Name 1")
        val user2 = randomUser(id = "user2", name = "Old Name 2")

        val updatedUser1 = randomUser(id = "user1", name = "New Name 1")
        val updatedUser2 = randomUser(id = "user2", name = "New Name 2")

        val message = randomMessage(user = user1)
        val member = randomMember(user = user1)
        val watcher = user2

        val channel = randomChannel(
            messages = listOf(message),
            members = listOf(member),
            watchers = listOf(watcher),
            createdBy = user1,
        )

        val updatedUsers = mapOf(
            "user1" to updatedUser1,
            "user2" to updatedUser2,
        )

        // when
        val result = channel.updateUsers(updatedUsers)

        // then
        // Check createdBy was updated
        result.createdBy.name shouldBeEqualTo "New Name 1"

        // Check messages were updated
        val updatedMessage = result.messages.first()
        updatedMessage.user.name shouldBeEqualTo "New Name 1"

        // Check members were updated
        val updatedMember = result.members.first()
        updatedMember.user.name shouldBeEqualTo "New Name 1"

        // Check watchers were updated
        val updatedWatcher = result.watchers.first()
        updatedWatcher.name shouldBeEqualTo "New Name 2"
    }

    @Test
    fun `should update activeLiveLocations for each channel based on matching cid`() {
        val channel1 = randomChannel(id = "channel1")
        val channel2 = randomChannel(id = "channel2")
        val channels = listOf(channel1, channel2)

        val location1 = randomLocation(cid = "${channel1.type}:channel1")
        val location2 = randomLocation(cid = "${channel2.type}:channel2")
        val location3 = randomLocation(cid = "${channel1.type}:channel1")
        val locations = listOf(location1, location2, location3)

        val updatedChannels = channels.updateLiveLocations(locations)

        val updatedChannel1 = updatedChannels.first { it.id == "channel1" }
        val updatedChannel2 = updatedChannels.first { it.id == "channel2" }

        assertEquals(listOf(location1, location3), updatedChannel1.activeLiveLocations)
        assertEquals(listOf(location2), updatedChannel2.activeLiveLocations)
    }

    @Test
    fun `should set activeLiveLocations to empty if no locations match`() {
        val channel = randomChannel(id = "channel1", activeLiveLocations = listOf(randomLocation(cid = "channel1")))
        val locations = listOf(randomLocation(cid = "channel2"))
        val updatedChannels = listOf(channel).updateLiveLocations(locations)
        assertEquals(emptyList<Location>(), updatedChannels.first().activeLiveLocations)
    }

    @Test
    fun `should not modify original channels`() {
        val channel = randomChannel(id = "channel1", activeLiveLocations = listOf(Location(cid = "channel1")))
        val locations = listOf(randomLocation(cid = "channel1"))
        val channels = listOf(channel)
        channels.updateLiveLocations(locations)
        assertEquals(listOf(Location(cid = "channel1")), channel.activeLiveLocations)
    }
}
