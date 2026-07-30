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
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * Tests for the on-device unread-count tracking enabled via
 * [io.getstream.chat.android.client.api.ChatClientConfig.isLocalUnreadCountEnabled].
 */
internal class ChannelStateImplLocalUnreadCountTest : ChannelStateImplTestBase() {

    @Test
    fun `markRead resets unread count locally when local tracking is enabled and read events are disabled`() =
        runTest {
            val state = localTrackingState()
            state.setChannelConfig(Config(readEventsEnabled = false))
            state.setMessages(listOf(createMessage(1, timestamp = 5000)))
            state.updateRead(createRead(currentUser, unreadMessages = 5, lastRead = Date(1000)))
            // when
            val result = state.markRead()
            // then: handled on-device and the local count is reset
            assertEquals(MarkReadResult.HandledLocally, result)
            assertEquals(0, state.unreadCount.value)
        }

    @Test
    fun `markRead is ignored when local tracking is disabled and read events are disabled`() = runTest {
        val state = localTrackingState(isLocalUnreadCountEnabled = false)
        state.setChannelConfig(Config(readEventsEnabled = false))
        state.setMessages(listOf(createMessage(1, timestamp = 5000)))
        state.updateRead(createRead(currentUser, unreadMessages = 5, lastRead = Date(1000)))
        // when
        val result = state.markRead()
        // then: not handled and the count is left untouched
        assertEquals(MarkReadResult.NotNeeded, result)
        assertEquals(5, state.unreadCount.value)
    }

    @Test
    fun `markRead uses the remote path when read events are enabled even if local tracking is enabled`() = runTest {
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = true))
        state.setMessages(listOf(createMessage(1, timestamp = 5000)))
        state.updateRead(
            createRead(currentUser, unreadMessages = 5, lastRead = Date(1000), lastReadMessageId = "old_id"),
        )
        // when
        val result = state.markRead()
        // then: the remote path applies and the count is reset optimistically
        assertEquals(MarkReadResult.RemoteRequired, result)
        assertEquals(0, state.unreadCount.value)
    }

    @Test
    fun `updateCurrentUserRead creates a read state and increments when none exists for a local tracking channel`() =
        runTest {
            val state = localTrackingState()
            state.setChannelConfig(Config(readEventsEnabled = false))
            assertNull(state.read.value)
            val message = createMessage(1, user = randomUser(id = "other_user"))
            // when
            state.updateCurrentUserRead(Date(2000), message)
            // then
            assertEquals(1, state.read.value?.unreadMessages)
        }

    @Test
    fun `updateCurrentUserRead does not create a read state for own messages on a local tracking channel`() = runTest {
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = false))
        val message = createMessage(1, user = currentUser)
        // when
        state.updateCurrentUserRead(Date(2000), message)
        // then
        assertNull(state.read.value)
        assertEquals(0, state.unreadCount.value)
    }

    @Test
    fun `updateCurrentUserRead does not create a read state when local tracking is disabled`() = runTest {
        val state = localTrackingState(isLocalUnreadCountEnabled = false)
        state.setChannelConfig(Config(readEventsEnabled = false))
        val message = createMessage(1, user = randomUser(id = "other_user"))
        // when
        state.updateCurrentUserRead(Date(2000), message)
        // then
        assertNull(state.read.value)
    }

    @Test
    fun `local tracking counts only messages received after a mark read`() = runTest {
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = false))
        val otherUser = randomUser(id = "other_user")
        // first message arrives -> unread = 1 (the read is updated before the message is upserted,
        // mirroring the event handling order)
        val firstMessage = createMessage(1, timestamp = 1000, user = otherUser)
        state.updateCurrentUserRead(Date(1000), firstMessage)
        state.setMessages(listOf(firstMessage))
        assertEquals(1, state.unreadCount.value)
        // user opens the channel -> local mark read resets to 0
        state.markRead()
        assertEquals(0, state.unreadCount.value)
        // a later message arrives -> unread = 1 again, not 2
        val secondMessage = createMessage(2, timestamp = 5000, user = otherUser)
        state.updateCurrentUserRead(Date(5000), secondMessage)
        state.setMessages(listOf(firstMessage, secondMessage))
        assertEquals(1, state.unreadCount.value)
    }

    @Test
    fun `updateCurrentUserRead does not count a replayed event for a message already in the state`() = runTest {
        // After a restart the sync replays the events since the last sync, including the event of
        // the newest message already counted (and persisted) before the restart. That message is
        // already part of the state seeded from the database, so it must not be counted twice.
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = false))
        val otherUser = randomUser(id = "other_user")
        val message = createMessage(1, timestamp = 5000, user = otherUser)
        // Seeded from the database: the message and the read counting it
        state.setMessages(listOf(message))
        state.updateRead(
            createRead(
                user = currentUser,
                unreadMessages = 1,
                lastRead = Date(1000),
                lastReceivedEventDate = Date(5000),
            ),
        )
        // when: the sync replays the message event
        state.updateCurrentUserRead(Date(5000), message)
        // then: the count is not incremented again
        assertEquals(1, state.unreadCount.value)
    }

    @Test
    fun `updateCurrentUserRead does not create a read state when read events are enabled`() = runTest {
        // read events enabled server-side, so the server owns the read state
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = true))
        val message = createMessage(1, user = randomUser(id = "other_user"))
        // when
        state.updateCurrentUserRead(Date(2000), message)
        // then
        assertNull(state.read.value)
        assertEquals(0, state.unreadCount.value)
    }

    @Test
    fun `updateReads preserves the locally tracked read even when the server data is more recent`() = runTest {
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = false))
        state.updateRead(
            createRead(currentUser, unreadMessages = 3, lastRead = Date(1000)),
        )
        // when: the server sends a sync payload with unreadMessages = 0 and a newer event date
        val serverRead = createRead(
            user = currentUser,
            unreadMessages = 0,
            lastRead = Date(5000),
            lastReceivedEventDate = Date(5000),
            lastReadMessageId = "server_message_id",
        )
        state.updateReads(listOf(serverRead))
        // then: the locally tracked read state must be preserved
        val read = state.read.value
        assertEquals(3, read?.unreadMessages)
        assertEquals(Date(1000), read?.lastRead)
        assertNull(read?.lastReadMessageId)
    }

    @Test
    fun `updateReads preserves the locally tracked read on equal event dates`() = runTest {
        // Server reads carry lastReceivedEventDate = last_message_at, which ties with the local value
        // anchored to the same message - a recency merge would let the stale server count win here.
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = false))
        val lastMessageDate = Date(3000)
        state.updateRead(
            createRead(
                user = currentUser,
                unreadMessages = 3,
                lastRead = Date(1000),
                lastReceivedEventDate = lastMessageDate,
            ),
        )
        // when: the server read ties on lastReceivedEventDate and carries a stale count
        val serverRead = createRead(
            user = currentUser,
            unreadMessages = 0,
            lastRead = Date(1000),
            lastReceivedEventDate = lastMessageDate,
        )
        state.updateReads(listOf(serverRead))
        // then
        assertEquals(3, state.read.value?.unreadMessages)
    }

    @Test
    fun `updateReads merges delivered fields from the server for locally tracked reads`() = runTest {
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = false))
        state.updateRead(
            createRead(currentUser, unreadMessages = 3, lastRead = Date(1000)),
        )
        // when
        val serverRead = createRead(
            user = currentUser.copy(name = "Updated Name"),
            unreadMessages = 0,
            lastRead = Date(5000),
            lastDeliveredAt = Date(4000),
            lastDeliveredMessageId = "delivered_message_id",
        )
        state.updateReads(listOf(serverRead))
        // then: user info and delivered fields come from the server, the rest stays local
        val read = state.read.value
        assertEquals(3, read?.unreadMessages)
        assertEquals("Updated Name", read?.user?.name)
        assertEquals(Date(4000), read?.lastDeliveredAt)
        assertEquals("delivered_message_id", read?.lastDeliveredMessageId)
    }

    @Test
    fun `updateReads uses the server data when no local read exists for a locally tracked channel`() = runTest {
        // first channel load, no local read state yet
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = false))
        assertNull(state.read.value)
        // when
        state.updateReads(
            listOf(createRead(currentUser, unreadMessages = 5, lastRead = Date(1000))),
        )
        // then: the server value is authoritative on first load
        assertEquals(5, state.read.value?.unreadMessages)
    }

    @Test
    fun `updateReads uses more recent server data when read events are enabled`() = runTest {
        // even with the flag on, channels with read events enabled follow the server
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = true))
        state.updateRead(
            createRead(currentUser, unreadMessages = 9, lastRead = Date(1000)),
        )
        // when
        val serverRead = createRead(
            user = currentUser,
            unreadMessages = 1,
            lastRead = Date(5000),
            lastReceivedEventDate = Date(5000),
        )
        state.updateReads(listOf(serverRead))
        // then
        assertEquals(1, state.read.value?.unreadMessages)
    }

    @Test
    fun `updateReads uses more recent server data when local tracking is disabled`() = runTest {
        // flag off, the standard recency merge applies
        val state = localTrackingState(isLocalUnreadCountEnabled = false)
        state.setChannelConfig(Config(readEventsEnabled = false))
        state.updateRead(
            createRead(currentUser, unreadMessages = 7, lastRead = Date(1000)),
        )
        // when
        val serverRead = createRead(
            user = currentUser,
            unreadMessages = 0,
            lastRead = Date(5000),
            lastReceivedEventDate = Date(5000),
        )
        state.updateReads(listOf(serverRead))
        // then
        assertEquals(0, state.read.value?.unreadMessages)
    }

    @Test
    fun `updateReads does not affect other users reads for locally tracked channels`() = runTest {
        val state = localTrackingState()
        state.setChannelConfig(Config(readEventsEnabled = false))
        val otherUser = randomUser(id = "other_user")
        // when
        state.updateReads(
            listOf(createRead(otherUser, unreadMessages = 10, lastRead = Date(2000))),
        )
        // then
        val otherRead = state.reads.value.find { it.user.id == "other_user" }
        assertEquals(10, otherRead?.unreadMessages)
    }

    private fun localTrackingState(isLocalUnreadCountEnabled: Boolean = true) = ChannelStateImpl(
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        currentUser = userFlow,
        latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
        mutedUsers = MutableStateFlow(emptyList()),
        liveLocations = MutableStateFlow(emptyList()),
        messageLimit = null,
        isLocalUnreadCountEnabled = isLocalUnreadCountEnabled,
    )

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
