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

package io.getstream.chat.android.client.internal.state.plugin.state

import io.getstream.chat.android.client.api.MessageLimitConfig
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.internal.state.event.handler.internal.batch.BatchEvent
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

internal class StateRegistryTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var legacyStateRegistry: StateRegistry
    private lateinit var stateRegistry: StateRegistry
    private lateinit var userStateFlow: StateFlow<User?>
    private lateinit var latestUsers: StateFlow<Map<String, User>>
    private lateinit var mutedUsers: StateFlow<List<Mute>>
    private lateinit var activeLiveLocations: StateFlow<List<Location>>
    private lateinit var job: Job
    private lateinit var scope: TestScope
    private lateinit var messageLimitConfig: MessageLimitConfig

    @BeforeEach
    fun setUp() {
        userStateFlow = MutableStateFlow(null)
        latestUsers = MutableStateFlow(emptyMap())
        mutedUsers = MutableStateFlow(emptyList())
        activeLiveLocations = MutableStateFlow(emptyList())
        job = Job()
        scope = testCoroutines.scope
        messageLimitConfig = MessageLimitConfig()

        legacyStateRegistry = StateRegistry(
            userStateFlow = userStateFlow,
            latestUsers = latestUsers,
            activeLiveLocations = activeLiveLocations,
            job = job,
            now = { System.currentTimeMillis() },
            scope = scope,
            messageLimitConfig = messageLimitConfig,
            mutedUsers = mutedUsers,
            useLegacyChannelState = true,
        )

        stateRegistry = StateRegistry(
            userStateFlow = userStateFlow,
            latestUsers = latestUsers,
            activeLiveLocations = activeLiveLocations,
            job = job,
            now = { System.currentTimeMillis() },
            scope = scope,
            messageLimitConfig = messageLimitConfig,
            mutedUsers = mutedUsers,
            useLegacyChannelState = false,
        )
    }

    // region General tests (not related to legacy/non-legacy channel state)

    // -- QueryChannels --

    @Test
    fun `queryChannels should return same instance for same filter and sort`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")

        // When
        val state1 = stateRegistry.queryChannels(filter, sort)
        val state2 = stateRegistry.queryChannels(filter, sort)

        // Then
        assertSame(state1, state2)
    }

    @Test
    fun `queryChannels should return different instances for different filters`() {
        // Given
        val filter1 = Filters.eq("type", "messaging")
        val filter2 = Filters.eq("type", "livestream")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")

        // When
        val state1 = stateRegistry.queryChannels(filter1, sort)
        val state2 = stateRegistry.queryChannels(filter2, sort)

        // Then
        assertNotSame(state1, state2)
    }

    @Test
    fun `queryChannels should return different instances for different sorts`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort1 = QuerySortByField.descByName<Channel>("last_message_at")
        val sort2 = QuerySortByField.descByName<Channel>("created_at")

        // When
        val state1 = stateRegistry.queryChannels(filter, sort1)
        val state2 = stateRegistry.queryChannels(filter, sort2)

        // Then
        assertNotSame(state1, state2)
    }

    // -- QueryThreads --

    @Test
    fun `queryThreads should return same instance for same filter and sort combination`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsState1 = stateRegistry.queryThreads(filter, sort)
        val queryThreadsState2 = stateRegistry.queryThreads(filter, sort)

        // Then
        assertSame(queryThreadsState1, queryThreadsState2)
    }

    @Test
    fun `queryThreads should return different instances for different filters with same sort`() {
        // Given
        val filter1 = Filters.eq("channel_cid", "messaging:123")
        val filter2 = Filters.eq("channel_cid", "messaging:456")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsState1 = stateRegistry.queryThreads(filter1, sort)
        val queryThreadsState2 = stateRegistry.queryThreads(filter2, sort)

        // Then
        assertNotSame(queryThreadsState1, queryThreadsState2)
    }

    @Test
    fun `queryThreads should return different instances for same filter with different sorts`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort1 = QuerySortByField.descByName<Thread>("last_message_at")
        val sort2 = QuerySortByField.ascByName<Thread>("created_at")

        // When
        val queryThreadsState1 = stateRegistry.queryThreads(filter, sort1)
        val queryThreadsState2 = stateRegistry.queryThreads(filter, sort2)

        // Then
        assertNotSame(queryThreadsState1, queryThreadsState2)
    }

    // -- MutableQueryThreads --

    @Test
    fun `mutableQueryThreads should return same instance for same filter and sort combination`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val mutableState1 = stateRegistry.mutableQueryThreads(filter, sort)
        val mutableState2 = stateRegistry.mutableQueryThreads(filter, sort)

        // Then
        assertSame(mutableState1, mutableState2)
    }

    @Test
    fun `mutableQueryThreads should return different instances for different filters with same sort`() {
        // Given
        val filter1 = Filters.eq("channel_cid", "messaging:123")
        val filter2 = Filters.eq("channel_cid", "messaging:456")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val mutableState1 = stateRegistry.mutableQueryThreads(filter1, sort)
        val mutableState2 = stateRegistry.mutableQueryThreads(filter2, sort)

        // Then
        assertNotSame(mutableState1, mutableState2)
    }

    @Test
    fun `mutableQueryThreads should return different instances for same filter with different sorts`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort1 = QuerySortByField.descByName<Thread>("last_message_at")
        val sort2 = QuerySortByField.ascByName<Thread>("created_at")

        // When
        val mutableState1 = stateRegistry.mutableQueryThreads(filter, sort1)
        val mutableState2 = stateRegistry.mutableQueryThreads(filter, sort2)

        // Then
        assertNotSame(mutableState1, mutableState2)
    }

    @Test
    fun `mutableQueryThreads should return different instances for null vs non-null filter with same sort`() {
        // Given
        val filter1: FilterObject? = null
        val filter2 = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val mutableState1 = stateRegistry.mutableQueryThreads(filter1, sort)
        val mutableState2 = stateRegistry.mutableQueryThreads(filter2, sort)

        // Then
        assertNotSame(mutableState1, mutableState2)
    }

    @Test
    fun `queryThreads and mutableQueryThreads should return same underlying instance`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsState = stateRegistry.queryThreads(filter, sort)
        val mutableState = stateRegistry.mutableQueryThreads(filter, sort)

        // Then
        assertSame(queryThreadsState, mutableState)
    }

    // -- Thread / MutableThread --

    @Test
    fun `thread should return same instance for same messageId`() {
        // When
        val thread1 = stateRegistry.thread("msg1")
        val thread2 = stateRegistry.thread("msg1")

        // Then
        assertSame(thread1, thread2)
    }

    @Test
    fun `thread should return different instances for different messageIds`() {
        // When
        val thread1 = stateRegistry.thread("msg1")
        val thread2 = stateRegistry.thread("msg2")

        // Then
        assertNotSame(thread1, thread2)
    }

    @Test
    fun `mutableThread should return same instance for same messageId`() {
        // When
        val mutableThread1 = stateRegistry.mutableThread("msg1")
        val mutableThread2 = stateRegistry.mutableThread("msg1")

        // Then
        assertSame(mutableThread1, mutableThread2)
    }

    @Test
    fun `mutableThread should return different instances for different messageIds`() {
        // When
        val mutableThread1 = stateRegistry.mutableThread("msg1")
        val mutableThread2 = stateRegistry.mutableThread("msg2")

        // Then
        assertNotSame(mutableThread1, mutableThread2)
    }

    @Test
    fun `thread and mutableThread should return same underlying instance`() {
        // When
        val threadState = stateRegistry.thread("msg1")
        val mutableThreadState = stateRegistry.mutableThread("msg1")

        // Then
        assertSame(threadState, mutableThreadState)
    }

    // -- Clear (shared state) --

    @Test
    fun `clear should remove queryChannels state`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val stateBefore = stateRegistry.queryChannels(filter, sort)

        // When
        stateRegistry.clear()

        // Then - a new instance should be created after clear
        val stateAfter = stateRegistry.queryChannels(filter, sort)
        assertNotSame(stateBefore, stateAfter)
    }

    @Test
    fun `clear should remove threads state`() {
        // Given
        val threadBefore = stateRegistry.mutableThread("msg1")

        // When
        stateRegistry.clear()

        // Then - a new instance should be created after clear
        val threadAfter = stateRegistry.mutableThread("msg1")
        assertNotSame(threadBefore, threadAfter)
    }

    @Test
    fun `clear should remove queryThreads state`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")
        val stateBefore = stateRegistry.mutableQueryThreads(filter, sort)

        // When
        stateRegistry.clear()

        // Then - a new instance should be created after clear
        val stateAfter = stateRegistry.mutableQueryThreads(filter, sort)
        assertNotSame(stateBefore, stateAfter)
    }

    // endregion

    // region Legacy ChannelState tests (useLegacyChannelState = true)

    @Test
    fun `legacy channel should return ChannelStateLegacyImpl instance`() {
        // When
        val channelState = legacyStateRegistry.channel("messaging", "123")

        // Then
        assertInstanceOf(ChannelStateLegacyImpl::class.java, channelState)
    }

    @Test
    fun `legacy channel cid should match the type and id`() {
        // When
        val channelState = legacyStateRegistry.channel("messaging", "123")

        // Then
        assertEquals("messaging:123", channelState.cid)
    }

    @Test
    fun `legacy channel should return same instance for same type and id`() {
        // When
        val state1 = legacyStateRegistry.channel("messaging", "123")
        val state2 = legacyStateRegistry.channel("messaging", "123")

        // Then
        assertSame(state1, state2)
    }

    @Test
    fun `legacy channel should return different instances for different channel ids`() {
        // When
        val state1 = legacyStateRegistry.channel("messaging", "123")
        val state2 = legacyStateRegistry.channel("messaging", "456")

        // Then
        assertNotSame(state1, state2)
    }

    @Test
    fun `legacy channel should return different instances for different channel types`() {
        // When
        val state1 = legacyStateRegistry.channel("messaging", "123")
        val state2 = legacyStateRegistry.channel("livestream", "123")

        // Then
        assertNotSame(state1, state2)
    }

    @Test
    fun `legacy isActiveChannel should return true after channel is created`() {
        // Given
        legacyStateRegistry.channel("messaging", "123")

        // Then
        assertTrue(legacyStateRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `legacy isActiveChannel should return false for non-existent channel`() {
        // Then
        assertFalse(legacyStateRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `legacy getActiveChannelStates should return empty list initially`() {
        // When
        val activeStates = legacyStateRegistry.getActiveChannelStates()

        // Then
        assertTrue(activeStates.isEmpty())
    }

    @Test
    fun `legacy getActiveChannelStates should return all created channels`() {
        // Given
        val state1 = legacyStateRegistry.channel("messaging", "123")
        val state2 = legacyStateRegistry.channel("messaging", "456")
        val state3 = legacyStateRegistry.channel("livestream", "789")

        // When
        val activeStates = legacyStateRegistry.getActiveChannelStates()

        // Then
        assertEquals(3, activeStates.size)
        assertTrue(activeStates.contains(state1))
        assertTrue(activeStates.contains(state2))
        assertTrue(activeStates.contains(state3))
    }

    @Test
    fun `legacy getActiveChannelStates should not duplicate states for same channel`() {
        // Given
        val state1 = legacyStateRegistry.channel("messaging", "123")
        val state2 = legacyStateRegistry.channel("messaging", "123")

        // When
        val activeStates = legacyStateRegistry.getActiveChannelStates()

        // Then
        assertEquals(1, activeStates.size)
        assertSame(state1, state2)
    }

    @Test
    fun `legacy legacyChannelState should return same instance as channel`() {
        // When
        val fromChannel = legacyStateRegistry.channel("messaging", "123")
        val fromLegacyChannelState = legacyStateRegistry.legacyChannelState("messaging", "123")

        // Then
        assertSame(fromChannel, fromLegacyChannelState)
    }

    @Test
    fun `legacy clear should remove all channel states`() {
        // Given
        legacyStateRegistry.channel("messaging", "123")
        legacyStateRegistry.channel("messaging", "456")
        assertEquals(2, legacyStateRegistry.getActiveChannelStates().size)

        // When
        legacyStateRegistry.clear()

        // Then
        assertTrue(legacyStateRegistry.getActiveChannelStates().isEmpty())
        assertFalse(legacyStateRegistry.isActiveChannel("messaging", "123"))
        assertFalse(legacyStateRegistry.isActiveChannel("messaging", "456"))
    }

    @Test
    fun `legacy clear should not affect channel states created after clear`() {
        // Given
        legacyStateRegistry.channel("messaging", "123")
        legacyStateRegistry.clear()

        // When
        val newState = legacyStateRegistry.channel("messaging", "123")

        // Then
        assertTrue(legacyStateRegistry.isActiveChannel("messaging", "123"))
        assertEquals(1, legacyStateRegistry.getActiveChannelStates().size)
        assertEquals("messaging:123", newState.cid)
    }

    @Test
    fun `legacy handleBatchEvent with ChannelDeletedEvent should remove channel`() {
        // Given
        legacyStateRegistry.channel("messaging", "123")
        assertTrue(legacyStateRegistry.isActiveChannel("messaging", "123"))

        val event = ChannelDeletedEvent(
            type = "channel.deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            channel = Channel(id = "123", type = "messaging"),
            user = null,
        )
        val batchEvent = BatchEvent(sortedEvents = listOf(event), isFromHistorySync = false)

        // When
        legacyStateRegistry.handleBatchEvent(batchEvent)

        // Then
        assertFalse(legacyStateRegistry.isActiveChannel("messaging", "123"))
        assertTrue(legacyStateRegistry.getActiveChannelStates().isEmpty())
    }

    @Test
    fun `legacy handleBatchEvent with NotificationChannelDeletedEvent should remove channel`() {
        // Given
        legacyStateRegistry.channel("messaging", "123")
        assertTrue(legacyStateRegistry.isActiveChannel("messaging", "123"))

        val event = NotificationChannelDeletedEvent(
            type = "notification.channel_deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            channel = Channel(id = "123", type = "messaging"),
        )
        val batchEvent = BatchEvent(sortedEvents = listOf(event), isFromHistorySync = false)

        // When
        legacyStateRegistry.handleBatchEvent(batchEvent)

        // Then
        assertFalse(legacyStateRegistry.isActiveChannel("messaging", "123"))
        assertTrue(legacyStateRegistry.getActiveChannelStates().isEmpty())
    }

    @Test
    fun `legacy handleBatchEvent with multiple delete events should remove all targeted channels`() {
        // Given
        legacyStateRegistry.channel("messaging", "123")
        legacyStateRegistry.channel("messaging", "456")
        legacyStateRegistry.channel("livestream", "789")
        assertEquals(3, legacyStateRegistry.getActiveChannelStates().size)

        val event1 = ChannelDeletedEvent(
            type = "channel.deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            channel = Channel(id = "123", type = "messaging"),
            user = null,
        )
        val event2 = NotificationChannelDeletedEvent(
            type = "notification.channel_deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "livestream:789",
            channelType = "livestream",
            channelId = "789",
            channel = Channel(id = "789", type = "livestream"),
        )
        val batchEvent = BatchEvent(sortedEvents = listOf(event1, event2), isFromHistorySync = false)

        // When
        legacyStateRegistry.handleBatchEvent(batchEvent)

        // Then
        assertFalse(legacyStateRegistry.isActiveChannel("messaging", "123"))
        assertTrue(legacyStateRegistry.isActiveChannel("messaging", "456"))
        assertFalse(legacyStateRegistry.isActiveChannel("livestream", "789"))
        assertEquals(1, legacyStateRegistry.getActiveChannelStates().size)
    }

    // endregion

    // region Non-legacy ChannelState tests (useLegacyChannelState = false)

    @Test
    fun `channel should return ChannelStateImpl instance`() {
        // When
        val channelState = stateRegistry.channel("messaging", "123")

        // Then
        assertInstanceOf(ChannelStateImpl::class.java, channelState)
    }

    @Test
    fun `channel cid should match the type and id`() {
        // When
        val channelState = stateRegistry.channel("messaging", "123")

        // Then
        assertEquals("messaging:123", channelState.cid)
    }

    @Test
    fun `channel should return same instance for same type and id`() {
        // When
        val state1 = stateRegistry.channel("messaging", "123")
        val state2 = stateRegistry.channel("messaging", "123")

        // Then
        assertSame(state1, state2)
    }

    @Test
    fun `channel should return different instances for different channel ids`() {
        // When
        val state1 = stateRegistry.channel("messaging", "123")
        val state2 = stateRegistry.channel("messaging", "456")

        // Then
        assertNotSame(state1, state2)
    }

    @Test
    fun `channel should return different instances for different channel types`() {
        // When
        val state1 = stateRegistry.channel("messaging", "123")
        val state2 = stateRegistry.channel("livestream", "123")

        // Then
        assertNotSame(state1, state2)
    }

    @Test
    fun `isActiveChannel should return true after channel is created`() {
        // Given
        stateRegistry.channel("messaging", "123")

        // Then
        assertTrue(stateRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `isActiveChannel should return false for non-existent channel`() {
        // Then
        assertFalse(stateRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `getActiveChannelStates should return empty list initially`() {
        // When
        val activeStates = stateRegistry.getActiveChannelStates()

        // Then
        assertTrue(activeStates.isEmpty())
    }

    @Test
    fun `getActiveChannelStates should return all created channels`() {
        // Given
        val state1 = stateRegistry.channel("messaging", "123")
        val state2 = stateRegistry.channel("messaging", "456")
        val state3 = stateRegistry.channel("livestream", "789")

        // When
        val activeStates = stateRegistry.getActiveChannelStates()

        // Then
        assertEquals(3, activeStates.size)
        assertTrue(activeStates.contains(state1))
        assertTrue(activeStates.contains(state2))
        assertTrue(activeStates.contains(state3))
    }

    @Test
    fun `getActiveChannelStates should not duplicate states for same channel`() {
        // Given
        val state1 = stateRegistry.channel("messaging", "123")
        val state2 = stateRegistry.channel("messaging", "123")

        // When
        val activeStates = stateRegistry.getActiveChannelStates()

        // Then
        assertEquals(1, activeStates.size)
        assertSame(state1, state2)
    }

    @Test
    fun `channelState should return same instance as channel`() {
        // When
        val fromChannel = stateRegistry.channel("messaging", "123")
        val fromChannelState = stateRegistry.channelState("messaging", "123")

        // Then
        assertSame(fromChannel, fromChannelState)
    }

    @Test
    fun `clear should remove all channel states`() {
        // Given
        stateRegistry.channel("messaging", "123")
        stateRegistry.channel("messaging", "456")
        assertEquals(2, stateRegistry.getActiveChannelStates().size)

        // When
        stateRegistry.clear()

        // Then
        assertTrue(stateRegistry.getActiveChannelStates().isEmpty())
        assertFalse(stateRegistry.isActiveChannel("messaging", "123"))
        assertFalse(stateRegistry.isActiveChannel("messaging", "456"))
    }

    @Test
    fun `handleBatchEvent with ChannelDeletedEvent should remove channel`() {
        // Given
        stateRegistry.channel("messaging", "123")
        assertTrue(stateRegistry.isActiveChannel("messaging", "123"))

        val event = ChannelDeletedEvent(
            type = "channel.deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            channel = Channel(id = "123", type = "messaging"),
            user = null,
        )
        val batchEvent = BatchEvent(sortedEvents = listOf(event), isFromHistorySync = false)

        // When
        stateRegistry.handleBatchEvent(batchEvent)

        // Then
        assertFalse(stateRegistry.isActiveChannel("messaging", "123"))
        assertTrue(stateRegistry.getActiveChannelStates().isEmpty())
    }

    @Test
    fun `handleBatchEvent with NotificationChannelDeletedEvent should remove channel`() {
        // Given
        stateRegistry.channel("messaging", "123")
        assertTrue(stateRegistry.isActiveChannel("messaging", "123"))

        val event = NotificationChannelDeletedEvent(
            type = "notification.channel_deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            channel = Channel(id = "123", type = "messaging"),
        )
        val batchEvent = BatchEvent(sortedEvents = listOf(event), isFromHistorySync = false)

        // When
        stateRegistry.handleBatchEvent(batchEvent)

        // Then
        assertFalse(stateRegistry.isActiveChannel("messaging", "123"))
        assertTrue(stateRegistry.getActiveChannelStates().isEmpty())
    }

    @Test
    fun `handleBatchEvent with multiple delete events should remove all targeted channels`() {
        // Given
        stateRegistry.channel("messaging", "123")
        stateRegistry.channel("messaging", "456")
        stateRegistry.channel("livestream", "789")
        assertEquals(3, stateRegistry.getActiveChannelStates().size)

        val event1 = ChannelDeletedEvent(
            type = "channel.deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            channel = Channel(id = "123", type = "messaging"),
            user = null,
        )
        val event2 = NotificationChannelDeletedEvent(
            type = "notification.channel_deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "livestream:789",
            channelType = "livestream",
            channelId = "789",
            channel = Channel(id = "789", type = "livestream"),
        )
        val batchEvent = BatchEvent(sortedEvents = listOf(event1, event2), isFromHistorySync = false)

        // When
        stateRegistry.handleBatchEvent(batchEvent)

        // Then
        assertFalse(stateRegistry.isActiveChannel("messaging", "123"))
        assertTrue(stateRegistry.isActiveChannel("messaging", "456"))
        assertFalse(stateRegistry.isActiveChannel("livestream", "789"))
        assertEquals(1, stateRegistry.getActiveChannelStates().size)
    }

    // endregion
}
