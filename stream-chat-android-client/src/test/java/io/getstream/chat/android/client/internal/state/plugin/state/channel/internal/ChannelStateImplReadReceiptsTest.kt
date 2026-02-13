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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplReadReceiptsTest : ChannelStateImplTestBase() {

    // region UpdateRead

    @Nested
    inner class UpdateRead {

        @Test
        fun `updateRead should add read state for a user`() = runTest {
            // given
            val user = randomUser(id = "user_1")
            val read = createRead(user, unreadMessages = 5, lastRead = Date(1000))
            // when
            channelState.updateRead(read)
            // then
            val reads = channelState.reads.value
            assertEquals(1, reads.size)
            assertEquals("user_1", reads.first().user.id)
            assertEquals(5, reads.first().unreadMessages)
        }

        @Test
        fun `updateRead should update existing read state`() = runTest {
            // given
            val user = randomUser(id = "user_1")
            val initialRead = createRead(user, unreadMessages = 5, lastRead = Date(1000))
            channelState.updateRead(initialRead)
            // when
            val updatedRead = initialRead.copy(unreadMessages = 0, lastRead = Date(2000))
            channelState.updateRead(updatedRead)
            // then
            val reads = channelState.reads.value
            assertEquals(1, reads.size)
            assertEquals(0, reads.first().unreadMessages)
            assertEquals(Date(2000), reads.first().lastRead)
        }
    }

    // endregion

    // region UpdateReads

    @Nested
    inner class UpdateReads {

        @Test
        fun `updateReads should add multiple read states`() = runTest {
            // given
            val user1 = randomUser(id = "user_1")
            val user2 = randomUser(id = "user_2")
            val reads = listOf(
                createRead(user1, unreadMessages = 3, lastRead = Date(1000)),
                createRead(user2, unreadMessages = 7, lastRead = Date(2000)),
            )
            // when
            channelState.updateReads(reads)
            // then
            assertEquals(2, channelState.reads.value.size)
        }

        @Test
        fun `updateReads should update existing read states`() = runTest {
            // given
            val user1 = randomUser(id = "user_1")
            val user2 = randomUser(id = "user_2")
            val initialReads = listOf(
                createRead(user1, unreadMessages = 3, lastRead = Date(1000)),
                createRead(user2, unreadMessages = 7, lastRead = Date(2000)),
            )
            channelState.updateReads(initialReads)
            // when
            val updatedReads = listOf(
                createRead(user1, unreadMessages = 0, lastRead = Date(3000)),
            )
            channelState.updateReads(updatedReads)
            // then
            assertEquals(2, channelState.reads.value.size)
            val user1Read = channelState.reads.value.find { it.user.id == "user_1" }
            assertEquals(0, user1Read?.unreadMessages)
            assertEquals(Date(3000), user1Read?.lastRead)
        }

        @Test
        fun `updateReads should preserve local current user read when local is more recent`() = runTest {
            // given - set up a current user read with a recent event date
            val localRead = createRead(
                user = currentUser,
                unreadMessages = 5,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(5000), // more recent
            )
            channelState.updateRead(localRead)
            // when - server provides stale data
            val serverRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(500),
                lastReceivedEventDate = Date(3000), // older
            )
            channelState.updateReads(listOf(serverRead))
            // then - local unread count should be preserved
            val currentRead = channelState.read.value
            assertNotNull(currentRead)
            assertEquals(5, currentRead?.unreadMessages)
            assertEquals(Date(5000), currentRead?.lastReceivedEventDate)
        }

        @Test
        fun `updateReads should use server current user read when server is more recent`() = runTest {
            // given - set up a current user read with an old event date
            val localRead = createRead(
                user = currentUser,
                unreadMessages = 5,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(2000), // older
            )
            channelState.updateRead(localRead)
            // when - server provides newer data
            val serverRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(3000),
                lastReceivedEventDate = Date(5000), // more recent
            )
            channelState.updateReads(listOf(serverRead))
            // then - server data should be used
            val currentRead = channelState.read.value
            assertNotNull(currentRead)
            assertEquals(0, currentRead?.unreadMessages)
            assertEquals(Date(5000), currentRead?.lastReceivedEventDate)
        }

        @Test
        fun `updateReads should merge lastRead taking the max when local is preserved`() = runTest {
            // given
            val localRead = createRead(
                user = currentUser,
                unreadMessages = 3,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(5000), // local more recent
            )
            channelState.updateRead(localRead)
            // when - server has a newer lastRead but older event date
            val serverRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(2000), // newer lastRead
                lastReceivedEventDate = Date(3000), // older event
            )
            channelState.updateReads(listOf(serverRead))
            // then - lastRead should be max of both
            val currentRead = channelState.read.value
            assertEquals(Date(2000), currentRead?.lastRead)
        }

        @Test
        fun `updateReads should not merge other users reads`() = runTest {
            // given - set up current user and other user reads
            val otherUser = randomUser(id = "other_user")
            val localCurrentUserRead = createRead(
                user = currentUser,
                unreadMessages = 5,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(5000),
            )
            channelState.updateRead(localCurrentUserRead)
            // when - update with other user's read
            val otherUserRead = createRead(
                user = otherUser,
                unreadMessages = 10,
                lastRead = Date(2000),
                lastReceivedEventDate = Date(3000),
            )
            channelState.updateReads(listOf(otherUserRead))
            // then - other user's read should be set directly, not merged
            val otherRead = channelState.reads.value.find { it.user.id == "other_user" }
            assertEquals(10, otherRead?.unreadMessages)
        }

        @Test
        fun `updateReads should work when no current user read exists`() = runTest {
            // given - no reads at all
            assertTrue(channelState.reads.value.isEmpty())
            // when
            val otherUser = randomUser(id = "other_user")
            val reads = listOf(
                createRead(otherUser, unreadMessages = 3, lastRead = Date(1000)),
            )
            channelState.updateReads(reads)
            // then
            assertEquals(1, channelState.reads.value.size)
        }
    }

    // endregion

    // region UpdateCurrentUserRead

    @Nested
    inner class UpdateCurrentUserRead {

        @Test
        fun `updateCurrentUserRead should increment unread count`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 2,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            val message = createMessage(1, user = otherUser)
            // when
            channelState.updateCurrentUserRead(Date(2000), message)
            // then
            assertEquals(3, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should update lastReceivedEventDate`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            val message = createMessage(1, user = otherUser)
            val eventDate = Date(5000)
            // when
            channelState.updateCurrentUserRead(eventDate, message)
            // then
            assertEquals(eventDate, channelState.read.value?.lastReceivedEventDate)
        }

        @Test
        fun `updateCurrentUserRead should skip already processed message`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            val message = createMessage(1, user = otherUser)
            channelState.updateCurrentUserRead(Date(2000), message)
            // when - process same message again
            channelState.updateCurrentUserRead(Date(3000), message)
            // then - unread count should only be incremented once
            assertEquals(1, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should skip when channel is muted`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            channelState.setMuted(true)
            val otherUser = randomUser(id = "other_user")
            val message = createMessage(1, user = otherUser)
            // when
            channelState.updateCurrentUserRead(Date(2000), message)
            // then
            assertEquals(0, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should skip thread replies not shown in channel`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            val threadReply = createMessage(1, user = otherUser, parentId = "parent_1", showInChannel = false)
            // when
            channelState.updateCurrentUserRead(Date(2000), threadReply)
            // then
            assertEquals(0, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should not skip thread replies shown in channel`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            val threadReply = createMessage(1, user = otherUser, parentId = "parent_1", showInChannel = true)
            // when
            channelState.updateCurrentUserRead(Date(2000), threadReply)
            // then
            assertEquals(1, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should skip messages from current user`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val message = createMessage(1, user = currentUser)
            // when
            channelState.updateCurrentUserRead(Date(2000), message)
            // then
            assertEquals(0, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should skip messages from muted users`() = runTest {
            // given
            val mutedUser = randomUser(id = "muted_user")
            val mutedUsersFlow = MutableStateFlow(listOf(randomMute(target = mutedUser)))
            val stateWithMutedUsers = ChannelStateImpl(
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                currentUser = userFlow,
                latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
                mutedUsers = mutedUsersFlow,
                liveLocations = MutableStateFlow(emptyList()),
                messageLimit = null,
            )
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            stateWithMutedUsers.updateRead(initialRead)
            val message = createMessage(1, user = mutedUser)
            // when
            stateWithMutedUsers.updateCurrentUserRead(Date(2000), message)
            // then
            assertEquals(0, stateWithMutedUsers.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should skip shadowed messages`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            val shadowedMessage = createMessage(1, user = otherUser, shadowed = true)
            // when
            channelState.updateCurrentUserRead(Date(2000), shadowedMessage)
            // then
            assertEquals(0, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should skip silent messages`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            val silentMessage = createMessage(1, user = otherUser, silent = true)
            // when
            channelState.updateCurrentUserRead(Date(2000), silentMessage)
            // then
            assertEquals(0, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should skip outdated events`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(5000), // recent event date
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            val message = createMessage(1, user = otherUser)
            // when - event date is older than current read's lastReceivedEventDate
            channelState.updateCurrentUserRead(Date(3000), message)
            // then
            assertEquals(0, channelState.read.value?.unreadMessages)
        }

        @Test
        fun `updateCurrentUserRead should do nothing when no current user read exists`() = runTest {
            // given - no read state for current user
            assertNull(channelState.read.value)
            val otherUser = randomUser(id = "other_user")
            val message = createMessage(1, user = otherUser)
            // when
            channelState.updateCurrentUserRead(Date(2000), message)
            // then - should not throw and unread count should be 0
            assertEquals(0, channelState.unreadCount.value)
        }

        @Test
        fun `updateCurrentUserRead should increment unread count for multiple messages`() = runTest {
            // given
            val initialRead = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(1000),
            )
            channelState.updateRead(initialRead)
            val otherUser = randomUser(id = "other_user")
            // when
            val message1 = createMessage(1, user = otherUser)
            channelState.updateCurrentUserRead(Date(2000), message1)
            val message2 = createMessage(2, user = otherUser)
            channelState.updateCurrentUserRead(Date(3000), message2)
            val message3 = createMessage(3, user = otherUser)
            channelState.updateCurrentUserRead(Date(4000), message3)
            // then
            assertEquals(3, channelState.read.value?.unreadMessages)
        }
    }

    // endregion

    // region MarkRead

    @Nested
    inner class MarkRead {

        @Test
        fun `markRead should return false when read events are disabled`() = runTest {
            // given
            channelState.setChannelConfig(Config(readEventsEnabled = false))
            channelState.setMessages(listOf(createMessage(1)))
            // when
            val result = channelState.markRead()
            // then
            assertFalse(result)
        }

        @Test
        fun `markRead should return true when no messages exist`() = runTest {
            // given
            channelState.setChannelConfig(Config(readEventsEnabled = true))
            // when
            val result = channelState.markRead()
            // then
            assertTrue(result)
        }

        @Test
        fun `markRead should return true when no read state for current user`() = runTest {
            // given
            channelState.setChannelConfig(Config(readEventsEnabled = true))
            channelState.setMessages(listOf(createMessage(1)))
            // when
            val result = channelState.markRead()
            // then
            assertTrue(result)
        }

        @Test
        fun `markRead should mark as read and reset unread count`() = runTest {
            // given
            channelState.setChannelConfig(Config(readEventsEnabled = true))
            val message = createMessage(1, timestamp = 5000)
            channelState.setMessages(listOf(message))
            val read = createRead(
                user = currentUser,
                unreadMessages = 5,
                lastRead = Date(1000),
                lastReadMessageId = "old_message_id",
            )
            channelState.updateRead(read)
            // when
            val result = channelState.markRead()
            // then
            assertTrue(result)
            assertEquals(0, channelState.unreadCount.value)
        }

        @Test
        fun `markRead should return false when last message is already read`() = runTest {
            // given
            channelState.setChannelConfig(Config(readEventsEnabled = true))
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            val read = createRead(
                user = currentUser,
                unreadMessages = 0,
                lastRead = Date(1000),
                lastReadMessageId = message.id, // same as last message
            )
            channelState.updateRead(read)
            // when
            val result = channelState.markRead()
            // then
            assertFalse(result)
        }

        @Test
        fun `markRead should update lastRead to last message createdAt`() = runTest {
            // given
            channelState.setChannelConfig(Config(readEventsEnabled = true))
            val message1 = createMessage(1, timestamp = 1000)
            val message2 = createMessage(2, timestamp = 2000)
            channelState.setMessages(listOf(message1, message2))
            val read = createRead(
                user = currentUser,
                unreadMessages = 2,
                lastRead = Date(500),
                lastReadMessageId = null,
            )
            channelState.updateRead(read)
            // when
            channelState.markRead()
            // then
            val currentRead = channelState.read.value
            assertEquals(Date(2000), currentRead?.lastRead)
        }
    }

    // endregion

    // region UpdateDelivered

    @Nested
    inner class UpdateDelivered {

        @Test
        fun `updateDelivered should update delivered fields on existing read`() = runTest {
            // given
            val user = randomUser(id = "user_1")
            val initialRead = createRead(
                user = user,
                unreadMessages = 3,
                lastRead = Date(1000),
            )
            channelState.updateRead(initialRead)
            // when
            val deliveredAt = Date(2000)
            val deliveredRead = randomChannelUserRead(
                user = user,
                lastReceivedEventDate = Date(2000),
                lastDeliveredAt = deliveredAt,
                lastDeliveredMessageId = "delivered_msg_1",
            )
            channelState.updateDelivered(deliveredRead)
            // then
            val updatedRead = channelState.reads.value.find { it.user.id == "user_1" }
            assertNotNull(updatedRead)
            assertEquals(deliveredAt, updatedRead?.lastDeliveredAt)
            assertEquals("delivered_msg_1", updatedRead?.lastDeliveredMessageId)
            // Verify that existing fields (like unreadMessages, lastRead) are preserved
            assertEquals(3, updatedRead?.unreadMessages)
            assertEquals(Date(1000), updatedRead?.lastRead)
        }

        @Test
        fun `updateDelivered should add new read if user has no existing read`() = runTest {
            // given - no reads exist
            assertTrue(channelState.reads.value.isEmpty())
            // when
            val user = randomUser(id = "user_1")
            val deliveredRead = randomChannelUserRead(
                user = user,
                lastReceivedEventDate = Date(1000),
                lastDeliveredAt = Date(2000),
                lastDeliveredMessageId = "delivered_msg_1",
            )
            channelState.updateDelivered(deliveredRead)
            // then
            assertEquals(1, channelState.reads.value.size)
            val read = channelState.reads.value.first()
            assertEquals("user_1", read.user.id)
            assertEquals(Date(2000), read.lastDeliveredAt)
        }

        @Test
        fun `updateDelivered should update user info`() = runTest {
            // given
            val user = randomUser(id = "user_1", name = "Old Name")
            val initialRead = createRead(user = user, unreadMessages = 0, lastRead = Date(1000))
            channelState.updateRead(initialRead)
            // when
            val updatedUser = user.copy(name = "New Name")
            val deliveredRead = randomChannelUserRead(
                user = updatedUser,
                lastReceivedEventDate = Date(2000),
                lastDeliveredAt = Date(2000),
                lastDeliveredMessageId = "delivered_msg_1",
            )
            channelState.updateDelivered(deliveredRead)
            // then
            val read = channelState.reads.value.find { it.user.id == "user_1" }
            assertEquals("New Name", read?.user?.name)
        }
    }

    // endregion

    // region ReadsStateFlow

    @Nested
    inner class ReadsStateFlow {

        @Test
        fun `reads should be sorted by lastRead ascending`() = runTest {
            // given
            val user1 = randomUser(id = "user_1")
            val user2 = randomUser(id = "user_2")
            val user3 = randomUser(id = "user_3")
            val reads = listOf(
                createRead(user2, unreadMessages = 0, lastRead = Date(2000)),
                createRead(user1, unreadMessages = 0, lastRead = Date(1000)),
                createRead(user3, unreadMessages = 0, lastRead = Date(3000)),
            )
            // when
            channelState.updateReads(reads)
            // then
            val sortedUserIds = channelState.reads.value.map { it.user.id }
            assertEquals(listOf("user_1", "user_2", "user_3"), sortedUserIds)
        }

        @Test
        fun `read should return current user read state`() = runTest {
            // given
            val otherUser = randomUser(id = "other_user")
            val reads = listOf(
                createRead(currentUser, unreadMessages = 5, lastRead = Date(1000)),
                createRead(otherUser, unreadMessages = 3, lastRead = Date(2000)),
            )
            // when
            channelState.updateReads(reads)
            // then
            val currentRead = channelState.read.value
            assertNotNull(currentRead)
            assertEquals(currentUser.id, currentRead?.user?.id)
            assertEquals(5, currentRead?.unreadMessages)
        }

        @Test
        fun `read should return null when current user has no read state`() = runTest {
            // given
            val otherUser = randomUser(id = "other_user")
            channelState.updateRead(
                createRead(otherUser, unreadMessages = 3, lastRead = Date(1000)),
            )
            // when & then
            assertNull(channelState.read.value)
        }

        @Test
        fun `unreadCount should reflect current user unread messages`() = runTest {
            // given
            val read = createRead(currentUser, unreadMessages = 7, lastRead = Date(1000))
            // when
            channelState.updateRead(read)
            // then
            assertEquals(7, channelState.unreadCount.value)
        }

        @Test
        fun `unreadCount should be zero when no current user read exists`() = runTest {
            // given - no reads
            // when & then
            assertEquals(0, channelState.unreadCount.value)
        }

        @Test
        fun `unreadCount should update when read state changes`() = runTest {
            // given
            val read = createRead(currentUser, unreadMessages = 5, lastRead = Date(1000))
            channelState.updateRead(read)
            assertEquals(5, channelState.unreadCount.value)
            // when
            val updatedRead = read.copy(unreadMessages = 0, lastRead = Date(2000))
            channelState.updateRead(updatedRead)
            // then
            assertEquals(0, channelState.unreadCount.value)
        }
    }

    // endregion

    private fun createRead(
        user: User,
        unreadMessages: Int,
        lastRead: Date,
        lastReceivedEventDate: Date = lastRead,
        lastReadMessageId: String? = null,
        lastDeliveredAt: Date? = null,
        lastDeliveredMessageId: String? = null,
    ): ChannelUserRead = ChannelUserRead(
        user = user,
        lastReceivedEventDate = lastReceivedEventDate,
        unreadMessages = unreadMessages,
        lastRead = lastRead,
        lastReadMessageId = lastReadMessageId,
        lastDeliveredAt = lastDeliveredAt,
        lastDeliveredMessageId = lastDeliveredMessageId,
    )
}
