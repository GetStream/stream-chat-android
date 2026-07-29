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
        // first message arrives -> unread = 1
        val firstMessage = createMessage(1, timestamp = 1000, user = otherUser)
        state.setMessages(listOf(firstMessage))
        state.updateCurrentUserRead(Date(1000), firstMessage)
        assertEquals(1, state.unreadCount.value)
        // user opens the channel -> local mark read resets to 0
        state.markRead()
        assertEquals(0, state.unreadCount.value)
        // a later message arrives -> unread = 1 again, not 2
        val secondMessage = createMessage(2, timestamp = 5000, user = otherUser)
        state.setMessages(listOf(firstMessage, secondMessage))
        state.updateCurrentUserRead(Date(5000), secondMessage)
        assertEquals(1, state.unreadCount.value)
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
    ): ChannelUserRead = ChannelUserRead(
        user = user,
        lastReceivedEventDate = lastReceivedEventDate,
        unreadMessages = unreadMessages,
        lastRead = lastRead,
        lastReadMessageId = lastReadMessageId,
    )
}
