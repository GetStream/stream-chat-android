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

package io.getstream.chat.android.client.internal.state.plugin.logic.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelLogicImpl
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.legacy.ChannelLogicLegacyImpl
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.client.internal.state.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.client.internal.state.plugin.state.querythreads.internal.QueryThreadsMutableState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.whenever
import java.util.concurrent.ConcurrentHashMap

@Suppress("LargeClass")
internal class LogicRegistryTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var legacyLogicRegistry: LogicRegistry
    private lateinit var logicRegistry: LogicRegistry
    private lateinit var stateRegistry: StateRegistry
    private lateinit var clientState: ClientState
    private lateinit var mutableGlobalState: MutableGlobalState
    private lateinit var repos: RepositoryFacade
    private lateinit var client: ChatClient
    private lateinit var coroutineScope: TestScope

    private val queryChannelsStateCache:
        ConcurrentHashMap<Pair<FilterObject, QuerySorter<Channel>>, QueryChannelsMutableState> = ConcurrentHashMap()
    private val threadsStateCache:
        ConcurrentHashMap<Pair<FilterObject?, QuerySorter<Thread>>, QueryThreadsMutableState> = ConcurrentHashMap()
    private val channelStateMocks = ConcurrentHashMap<Pair<String, String>, ChannelStateImpl>()
    private val legacyChannelStateMocks = ConcurrentHashMap<Pair<String, String>, ChannelStateLegacyImpl>()
    private val threadStateMocks = ConcurrentHashMap<String, ThreadMutableState>()

    @Suppress("LongMethod")
    @BeforeEach
    fun setUp() {
        stateRegistry = mock()
        clientState = mock()
        mutableGlobalState = mock()
        repos = mock()
        client = mock()
        coroutineScope = testCoroutines.scope

        // Stub global state needed by legacy ChannelStateLogic.syncMuteState()
        whenever(mutableGlobalState.channelMutes).thenReturn(MutableStateFlow(emptyList()))

        // Stub repository methods used by thread() coroutine
        repos.stub {
            onBlocking { selectMessage(any()) } doReturn null
            onBlocking { selectMessagesForThread(any(), any()) } doReturn emptyList()
        }

        // Stub query channels state
        queryChannelsStateCache.clear()
        whenever(stateRegistry.queryChannels(any(), any())).thenAnswer {
            val filter = it.getArgument<FilterObject>(0)

            @Suppress("UNCHECKED_CAST")
            val sort = it.getArgument<QuerySorter<Channel>>(1)
            queryChannelsStateCache.getOrPut(filter to sort) {
                QueryChannelsMutableState(
                    filter = filter,
                    sort = sort,
                    scope = coroutineScope,
                    latestUsers = MutableStateFlow(emptyMap()),
                    activeLiveLocations = MutableStateFlow(emptyList()),
                )
            }
        }

        // Stub query threads state
        whenever(stateRegistry.mutableQueryThreads(anyOrNull(), any())).thenAnswer {
            val filter = it.getArgument<FilterObject?>(0)
            val sort = it.getArgument<QuerySortByField<Thread>>(1)
            threadsStateCache.getOrPut(filter to sort) {
                QueryThreadsMutableState(filter, sort)
            }
        }

        // Stub thread state
        threadStateMocks.clear()
        whenever(stateRegistry.mutableThread(any())).thenAnswer {
            val messageId = it.getArgument<String>(0)
            threadStateMocks.getOrPut(messageId) {
                ThreadMutableState(messageId, coroutineScope)
            }
        }

        // Stub new channel state (ChannelStateImpl mocks)
        channelStateMocks.clear()
        whenever(stateRegistry.channelState(any(), any())).thenAnswer {
            val type = it.getArgument<String>(0)
            val id = it.getArgument<String>(1)
            channelStateMocks.getOrPut(type to id) { mock() }
        }

        // Stub legacy channel state (real ChannelStateLegacyImpl instances)
        legacyChannelStateMocks.clear()
        whenever(stateRegistry.legacyChannelState(any(), any())).thenAnswer {
            val type = it.getArgument<String>(0)
            val id = it.getArgument<String>(1)
            legacyChannelStateMocks.getOrPut(type to id) {
                ChannelStateLegacyImpl(
                    channelType = type,
                    channelId = id,
                    userFlow = MutableStateFlow(null),
                    latestUsers = MutableStateFlow(emptyMap()),
                    activeLiveLocations = MutableStateFlow(emptyList()),
                    baseMessageLimit = null,
                    now = { System.currentTimeMillis() },
                )
            }
        }

        legacyLogicRegistry = LogicRegistry(
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            userPresence = true,
            repos = repos,
            client = client,
            coroutineScope = coroutineScope,
            now = { System.currentTimeMillis() },
            useLegacyChannelLogic = true,
        )

        logicRegistry = LogicRegistry(
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            userPresence = true,
            repos = repos,
            client = client,
            coroutineScope = coroutineScope,
            now = { System.currentTimeMillis() },
            useLegacyChannelLogic = false,
        )
    }

    // region General tests (not related to legacy channel logic)

    // -- QueryChannels --

    @Test
    fun `queryChannels should return same instance for same filter and sort`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")

        // When
        val logic1 = logicRegistry.queryChannels(filter, sort)
        val logic2 = logicRegistry.queryChannels(filter, sort)

        // Then
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return different instances for different filters`() {
        // Given
        val filter1 = Filters.eq("type", "messaging")
        val filter2 = Filters.eq("type", "livestream")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")

        // When
        val logic1 = logicRegistry.queryChannels(filter1, sort)
        val logic2 = logicRegistry.queryChannels(filter2, sort)

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return different instances for different sorts`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort1 = QuerySortByField.descByName<Channel>("last_message_at")
        val sort2 = QuerySortByField.descByName<Channel>("created_at")

        // When
        val logic1 = logicRegistry.queryChannels(filter, sort1)
        val logic2 = logicRegistry.queryChannels(filter, sort2)

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `queryChannels via request should return same instance as direct call with same filter and sort`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val request = QueryChannelsRequest(filter = filter, querySort = sort, limit = 30)

        // When
        val logic1 = logicRegistry.queryChannels(request)
        val logic2 = logicRegistry.queryChannels(filter, sort)

        // Then
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `getActiveQueryChannelsLogic should return empty list initially`() {
        // When
        val activeLogics = logicRegistry.getActiveQueryChannelsLogic()

        // Then
        Assertions.assertTrue(activeLogics.isEmpty())
    }

    @Test
    fun `getActiveQueryChannelsLogic should return all created query channels`() {
        // Given
        val filter1 = Filters.eq("type", "messaging")
        val filter2 = Filters.eq("type", "livestream")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val logic1 = logicRegistry.queryChannels(filter1, sort)
        val logic2 = logicRegistry.queryChannels(filter2, sort)

        // When
        val activeLogics = logicRegistry.getActiveQueryChannelsLogic()

        // Then
        Assertions.assertEquals(2, activeLogics.size)
        Assertions.assertTrue(activeLogics.contains(logic1))
        Assertions.assertTrue(activeLogics.contains(logic2))
    }

    @Test
    fun `clear should remove query channels`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        logicRegistry.queryChannels(filter, sort)
        Assertions.assertEquals(1, logicRegistry.getActiveQueryChannelsLogic().size)

        // When
        logicRegistry.clear()

        // Then
        Assertions.assertTrue(logicRegistry.getActiveQueryChannelsLogic().isEmpty())
    }

    // -- QueryThreads --

    @Test
    fun `threads methods should return same instance for default QueryThreadsRequest`() {
        // Given
        val request = QueryThreadsRequest()

        // When
        val queryThreadsLogic1 = logicRegistry.threads(request)
        val queryThreadsLogic2 = logicRegistry.threads(request)

        // Then
        Assertions.assertSame(queryThreadsLogic1, queryThreadsLogic2)
    }

    @Test
    fun `threads methods should return same instance for same filter and sort combination`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsLogic1 = logicRegistry.threads(filter, sort)
        val queryThreadsLogic2 = logicRegistry.threads(filter, sort)

        // Then
        Assertions.assertSame(queryThreadsLogic1, queryThreadsLogic2)
    }

    @Test
    fun `threads methods should return same instance when using QueryThreadsRequest with same filter and sort`() {
        // Given
        val filter = Filters.eq("parent_message_id", "msg123")
        val sort = QuerySortByField.descByName<Thread>("created_at")
        val queryThreadsRequest = QueryThreadsRequest(filter = filter, sort = sort)

        // When
        val queryThreadsLogic1 = logicRegistry.threads(queryThreadsRequest)
        val queryThreadsLogic2 = logicRegistry.threads(filter, sort)

        // Then
        Assertions.assertSame(queryThreadsLogic1, queryThreadsLogic2)
    }

    @Test
    fun `threads methods should return same instance for null filter and same sort`() {
        // Given
        val filter: FilterObject? = null
        val sort = QuerySortByField.descByName<Thread>("reply_count")

        // When
        val queryThreadsLogic1 = logicRegistry.threads(filter, sort)
        val queryThreadsLogic2 = logicRegistry.threads(filter, sort)

        // Then
        Assertions.assertSame(queryThreadsLogic1, queryThreadsLogic2)
    }

    @Test
    fun `threads methods should return same instance when QueryThreadsRequest has null filter`() {
        // Given
        val filter: FilterObject? = null
        val sort = QuerySortByField.descByName<Thread>("updated_at")
        val queryThreadsRequest = QueryThreadsRequest(filter = filter, sort = sort)

        // When
        val queryThreadsLogic1 = logicRegistry.threads(queryThreadsRequest)
        val queryThreadsLogic2 = logicRegistry.threads(filter, sort)

        // Then
        Assertions.assertSame(queryThreadsLogic1, queryThreadsLogic2)
    }

    @Test
    fun `threads methods should return different instances for different filters`() {
        // Given
        val filter1 = Filters.eq("channel_cid", "messaging:123")
        val filter2 = Filters.eq("channel_cid", "messaging:456")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsLogic1 = logicRegistry.threads(filter1, sort)
        val queryThreadsLogic2 = logicRegistry.threads(filter2, sort)

        // Then
        Assertions.assertNotSame(queryThreadsLogic1, queryThreadsLogic2)
    }

    @Test
    fun `threads methods should return different instances for different sorts`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort1 = QuerySortByField.descByName<Thread>("last_message_at")
        val sort2 = QuerySortByField.descByName<Thread>("created_at")

        // When
        val queryThreadsLogic1 = logicRegistry.threads(filter, sort1)
        val queryThreadsLogic2 = logicRegistry.threads(filter, sort2)

        // Then
        Assertions.assertNotSame(queryThreadsLogic1, queryThreadsLogic2)
    }

    @Test
    fun `threads methods should return different instances when one has null filter and other has non-null filter`() {
        // Given
        val filter1: FilterObject? = null
        val filter2 = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsLogic1 = logicRegistry.threads(filter1, sort)
        val queryThreadsLogic2 = logicRegistry.threads(filter2, sort)

        // Then
        Assertions.assertNotSame(queryThreadsLogic1, queryThreadsLogic2)
    }

    @Test
    fun `getActiveQueryThreadsLogic should return empty list initially`() {
        // When
        val activeLogics = logicRegistry.getActiveQueryThreadsLogic()

        // Then
        Assertions.assertTrue(activeLogics.isEmpty())
    }

    @Test
    fun `getActiveQueryThreadsLogic should return single logic after creating one`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsLogic = logicRegistry.threads(filter, sort)
        val activeLogics = logicRegistry.getActiveQueryThreadsLogic()

        // Then
        Assertions.assertEquals(1, activeLogics.size)
        Assertions.assertSame(queryThreadsLogic, activeLogics[0])
    }

    @Test
    fun `getActiveQueryThreadsLogic should return multiple logics after creating different ones`() {
        // Given
        val filter1 = Filters.eq("channel_cid", "messaging:123")
        val filter2 = Filters.eq("channel_cid", "messaging:456")
        val filter3: FilterObject? = null
        val sort1 = QuerySortByField.descByName<Thread>("last_message_at")
        val sort2 = QuerySortByField.descByName<Thread>("created_at")

        // When
        val queryThreadsLogic1 = logicRegistry.threads(filter1, sort1)
        val queryThreadsLogic2 = logicRegistry.threads(filter2, sort1)
        val queryThreadsLogic3 = logicRegistry.threads(filter3, sort2)
        val activeLogics = logicRegistry.getActiveQueryThreadsLogic()

        // Then
        Assertions.assertEquals(3, activeLogics.size)
        Assertions.assertTrue(activeLogics.contains(queryThreadsLogic1))
        Assertions.assertTrue(activeLogics.contains(queryThreadsLogic2))
        Assertions.assertTrue(activeLogics.contains(queryThreadsLogic3))
    }

    @Test
    fun `getActiveQueryThreadsLogic should not duplicate logics for same filter and sort`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsLogic1 = logicRegistry.threads(filter, sort)
        val queryThreadsLogic2 = logicRegistry.threads(filter, sort) // Same parameters
        val activeLogics = logicRegistry.getActiveQueryThreadsLogic()

        // Then
        Assertions.assertEquals(1, activeLogics.size)
        Assertions.assertSame(queryThreadsLogic1, queryThreadsLogic2)
        Assertions.assertSame(queryThreadsLogic1, activeLogics[0])
    }

    @Test
    fun `getActiveQueryThreadsLogic should include logics created via QueryThreadsRequest`() {
        // Given
        val filter = Filters.eq("parent_message_id", "msg123")
        val sort = QuerySortByField.descByName<Thread>("created_at")
        val queryThreadsRequest = QueryThreadsRequest(filter = filter, sort = sort)

        // When
        val queryThreadsLogic1 = logicRegistry.threads(queryThreadsRequest)
        val queryThreadsLogic2 = logicRegistry.threads(filter, sort)
        val activeLogics = logicRegistry.getActiveQueryThreadsLogic()

        // Then
        Assertions.assertEquals(1, activeLogics.size)
        Assertions.assertSame(queryThreadsLogic1, queryThreadsLogic2)
        Assertions.assertSame(queryThreadsLogic1, activeLogics[0])
    }

    @Test
    fun `getActiveQueryThreadsLogic should handle mixed creation methods`() {
        // Given
        val filter1 = Filters.eq("channel_cid", "messaging:123")
        val filter2 = Filters.eq("parent_message_id", "msg456")
        val sort1 = QuerySortByField.descByName<Thread>("last_message_at")
        val sort2 = QuerySortByField.descByName<Thread>("created_at")
        val queryThreadsRequest = QueryThreadsRequest(filter = filter2, sort = sort2)

        // When
        val queryThreadsLogic1 = logicRegistry.threads(filter1, sort1) // Direct method
        val queryThreadsLogic2 = logicRegistry.threads(queryThreadsRequest) // Via request
        val activeLogics = logicRegistry.getActiveQueryThreadsLogic()

        // Then
        Assertions.assertEquals(2, activeLogics.size)
        Assertions.assertTrue(activeLogics.contains(queryThreadsLogic1))
        Assertions.assertTrue(activeLogics.contains(queryThreadsLogic2))
    }

    @Test
    fun `thread should return same instance for same messageId`() {
        // When
        val thread1 = logicRegistry.thread("msg1")
        val thread2 = logicRegistry.thread("msg1")

        // Then
        Assertions.assertSame(thread1, thread2)
    }

    @Test
    fun `thread should return different instances for different messageIds`() {
        // When
        val thread1 = logicRegistry.thread("msg1")
        val thread2 = logicRegistry.thread("msg2")

        // Then
        Assertions.assertNotSame(thread1, thread2)
    }

    @Test
    fun `isActiveThread should return true after thread is created`() {
        // Given
        logicRegistry.thread("msg1")

        // Then
        Assertions.assertTrue(logicRegistry.isActiveThread("msg1"))
    }

    @Test
    fun `isActiveThread should return false for non-existent thread`() {
        // Then
        Assertions.assertFalse(logicRegistry.isActiveThread("msg1"))
    }

    @Test
    fun `threadFromMessage should return thread for message with parentId`() {
        // Given
        val message = Message(id = "reply1", cid = "messaging:123", parentId = "parent1")

        // When
        val result = logicRegistry.threadFromMessage(message)

        // Then
        Assertions.assertNotNull(result)
    }

    @Test
    fun `threadFromMessage should return null for message without parentId`() {
        // Given
        val message = Message(id = "msg1", cid = "messaging:123", parentId = null)

        // When
        val result = logicRegistry.threadFromMessage(message)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `threadFromMessage should create and cache the thread`() {
        // Given
        val message = Message(id = "reply1", cid = "messaging:123", parentId = "parent1")

        // When
        logicRegistry.threadFromMessage(message)

        // Then
        Assertions.assertTrue(logicRegistry.isActiveThread("parent1"))
    }

    @Test
    fun `getMessageById should return null when no channels or threads have the message`() {
        // When
        val result = logicRegistry.getMessageById("nonexistent")

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `clear should remove threads`() {
        // Given
        logicRegistry.thread("msg1")
        logicRegistry.thread("msg2")
        Assertions.assertTrue(logicRegistry.isActiveThread("msg1"))
        Assertions.assertTrue(logicRegistry.isActiveThread("msg2"))

        // When
        logicRegistry.clear()

        // Then
        Assertions.assertFalse(logicRegistry.isActiveThread("msg1"))
        Assertions.assertFalse(logicRegistry.isActiveThread("msg2"))
    }

    @Test
    fun `clear should remove query threads`() {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")
        logicRegistry.threads(filter, sort)
        Assertions.assertEquals(1, logicRegistry.getActiveQueryThreadsLogic().size)

        // When
        logicRegistry.clear()

        // Then
        Assertions.assertTrue(logicRegistry.getActiveQueryThreadsLogic().isEmpty())
    }

    // endregion

    // region Legacy ChannelLogic tests (useLegacyChannelLogic = true)

    @Test
    fun `legacy channel should return ChannelLogicLegacyImpl instance`() {
        // When
        val channelLogic = legacyLogicRegistry.channel("messaging", "123")

        // Then
        Assertions.assertInstanceOf(ChannelLogicLegacyImpl::class.java, channelLogic)
    }

    @Test
    fun `legacy channel cid should match the type and id`() {
        // When
        val channelLogic = legacyLogicRegistry.channel("messaging", "123")

        // Then
        Assertions.assertEquals("messaging:123", channelLogic.cid)
    }

    @Test
    fun `legacy channel should return same instance for same type and id`() {
        // When
        val logic1 = legacyLogicRegistry.channel("messaging", "123")
        val logic2 = legacyLogicRegistry.channel("messaging", "123")

        // Then
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `legacy channel should return different instances for different channel ids`() {
        // When
        val logic1 = legacyLogicRegistry.channel("messaging", "123")
        val logic2 = legacyLogicRegistry.channel("messaging", "456")

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `legacy channel should return different instances for different channel types`() {
        // When
        val logic1 = legacyLogicRegistry.channel("messaging", "123")
        val logic2 = legacyLogicRegistry.channel("livestream", "123")

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `legacy isActiveChannel should return true after channel is created`() {
        // Given
        legacyLogicRegistry.channel("messaging", "123")

        // Then
        Assertions.assertTrue(legacyLogicRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `legacy isActiveChannel should return false for non-existent channel`() {
        // Then
        Assertions.assertFalse(legacyLogicRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `legacy getActiveChannelsLogic should return empty list initially`() {
        // When
        val activeChannels = legacyLogicRegistry.getActiveChannelsLogic()

        // Then
        Assertions.assertTrue(activeChannels.isEmpty())
    }

    @Test
    fun `legacy getActiveChannelsLogic should return all created channels`() {
        // Given
        val logic1 = legacyLogicRegistry.channel("messaging", "123")
        val logic2 = legacyLogicRegistry.channel("messaging", "456")
        val logic3 = legacyLogicRegistry.channel("livestream", "789")

        // When
        val activeChannels = legacyLogicRegistry.getActiveChannelsLogic()

        // Then
        Assertions.assertEquals(3, activeChannels.size)
        Assertions.assertTrue(activeChannels.contains(logic1))
        Assertions.assertTrue(activeChannels.contains(logic2))
        Assertions.assertTrue(activeChannels.contains(logic3))
    }

    @Test
    fun `legacy getActiveChannelsLogic should not duplicate logics for same channel`() {
        // Given
        val logic1 = legacyLogicRegistry.channel("messaging", "123")
        val logic2 = legacyLogicRegistry.channel("messaging", "123")

        // When
        val activeChannels = legacyLogicRegistry.getActiveChannelsLogic()

        // Then
        Assertions.assertEquals(1, activeChannels.size)
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `legacy removeChannel should remove channel from registry`() {
        // Given
        legacyLogicRegistry.channel("messaging", "123")
        Assertions.assertTrue(legacyLogicRegistry.isActiveChannel("messaging", "123"))

        // When
        legacyLogicRegistry.removeChannel("messaging", "123")

        // Then
        Assertions.assertFalse(legacyLogicRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `legacy removeChannel should only remove specified channel`() {
        // Given
        legacyLogicRegistry.channel("messaging", "123")
        legacyLogicRegistry.channel("messaging", "456")

        // When
        legacyLogicRegistry.removeChannel("messaging", "123")

        // Then
        Assertions.assertFalse(legacyLogicRegistry.isActiveChannel("messaging", "123"))
        Assertions.assertTrue(legacyLogicRegistry.isActiveChannel("messaging", "456"))
    }

    @Test
    fun `legacy channelFromMessage should return logic for top-level message`() {
        // Given
        legacyLogicRegistry.channel("messaging", "123")
        val message = Message(id = "msg1", cid = "messaging:123", parentId = null)

        // When
        val result = legacyLogicRegistry.channelFromMessage(message)

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("messaging:123", result!!.cid)
    }

    @Test
    fun `legacy channelFromMessage should return logic for thread reply shown in channel`() {
        // Given
        legacyLogicRegistry.channel("messaging", "123")
        val message = Message(
            id = "msg1",
            cid = "messaging:123",
            parentId = "parent1",
            showInChannel = true,
        )

        // When
        val result = legacyLogicRegistry.channelFromMessage(message)

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("messaging:123", result!!.cid)
    }

    @Test
    fun `legacy channelFromMessage should return null for thread-only reply`() {
        // Given
        legacyLogicRegistry.channel("messaging", "123")
        val message = Message(
            id = "msg1",
            cid = "messaging:123",
            parentId = "parent1",
            showInChannel = false,
        )

        // When
        val result = legacyLogicRegistry.channelFromMessage(message)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `legacy clear should remove all channel logic entries`() {
        // Given
        legacyLogicRegistry.channel("messaging", "123")
        legacyLogicRegistry.channel("messaging", "456")
        Assertions.assertEquals(2, legacyLogicRegistry.getActiveChannelsLogic().size)

        // When
        legacyLogicRegistry.clear()

        // Then
        Assertions.assertTrue(legacyLogicRegistry.getActiveChannelsLogic().isEmpty())
        Assertions.assertFalse(legacyLogicRegistry.isActiveChannel("messaging", "123"))
        Assertions.assertFalse(legacyLogicRegistry.isActiveChannel("messaging", "456"))
    }

    // endregion

    // region Non-legacy ChannelLogic tests (useLegacyChannelLogic = false)

    @Test
    fun `channel should return ChannelLogicImpl instance`() {
        // When
        val channelLogic = logicRegistry.channel("messaging", "123")

        // Then
        Assertions.assertInstanceOf(ChannelLogicImpl::class.java, channelLogic)
    }

    @Test
    fun `channel cid should match the type and id`() {
        // When
        val channelLogic = logicRegistry.channel("messaging", "123")

        // Then
        Assertions.assertEquals("messaging:123", channelLogic.cid)
    }

    @Test
    fun `channel should return same instance for same type and id`() {
        // When
        val logic1 = logicRegistry.channel("messaging", "123")
        val logic2 = logicRegistry.channel("messaging", "123")

        // Then
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `channel should return different instances for different channel ids`() {
        // When
        val logic1 = logicRegistry.channel("messaging", "123")
        val logic2 = logicRegistry.channel("messaging", "456")

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `channel should return different instances for different channel types`() {
        // When
        val logic1 = logicRegistry.channel("messaging", "123")
        val logic2 = logicRegistry.channel("livestream", "123")

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `isActiveChannel should return true after channel is created`() {
        // Given
        logicRegistry.channel("messaging", "123")

        // Then
        Assertions.assertTrue(logicRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `isActiveChannel should return false for non-existent channel`() {
        // Then
        Assertions.assertFalse(logicRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `getActiveChannelsLogic should return empty list initially`() {
        // When
        val activeChannels = logicRegistry.getActiveChannelsLogic()

        // Then
        Assertions.assertTrue(activeChannels.isEmpty())
    }

    @Test
    fun `getActiveChannelsLogic should return all created channels`() {
        // Given
        val logic1 = logicRegistry.channel("messaging", "123")
        val logic2 = logicRegistry.channel("messaging", "456")
        val logic3 = logicRegistry.channel("livestream", "789")

        // When
        val activeChannels = logicRegistry.getActiveChannelsLogic()

        // Then
        Assertions.assertEquals(3, activeChannels.size)
        Assertions.assertTrue(activeChannels.contains(logic1))
        Assertions.assertTrue(activeChannels.contains(logic2))
        Assertions.assertTrue(activeChannels.contains(logic3))
    }

    @Test
    fun `getActiveChannelsLogic should not duplicate logics for same channel`() {
        // Given
        val logic1 = logicRegistry.channel("messaging", "123")
        val logic2 = logicRegistry.channel("messaging", "123")

        // When
        val activeChannels = logicRegistry.getActiveChannelsLogic()

        // Then
        Assertions.assertEquals(1, activeChannels.size)
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `removeChannel should remove channel from registry`() {
        // Given
        logicRegistry.channel("messaging", "123")
        Assertions.assertTrue(logicRegistry.isActiveChannel("messaging", "123"))

        // When
        logicRegistry.removeChannel("messaging", "123")

        // Then
        Assertions.assertFalse(logicRegistry.isActiveChannel("messaging", "123"))
    }

    @Test
    fun `removeChannel should only remove specified channel`() {
        // Given
        logicRegistry.channel("messaging", "123")
        logicRegistry.channel("messaging", "456")

        // When
        logicRegistry.removeChannel("messaging", "123")

        // Then
        Assertions.assertFalse(logicRegistry.isActiveChannel("messaging", "123"))
        Assertions.assertTrue(logicRegistry.isActiveChannel("messaging", "456"))
    }

    @Test
    fun `channelStateLogic should return messagesUpdateLogic from channel`() {
        // Given
        val channelLogic = logicRegistry.channel("messaging", "123")

        // When
        val stateLogic = logicRegistry.channelStateLogic("messaging", "123")

        // Then
        Assertions.assertSame(channelLogic.messagesUpdateLogic, stateLogic)
    }

    @Test
    fun `channelFromMessage should return logic for top-level message`() {
        // Given
        logicRegistry.channel("messaging", "123")
        val message = Message(id = "msg1", cid = "messaging:123", parentId = null)

        // When
        val result = logicRegistry.channelFromMessage(message)

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("messaging:123", result!!.cid)
    }

    @Test
    fun `channelFromMessage should return logic for thread reply shown in channel`() {
        // Given
        logicRegistry.channel("messaging", "123")
        val message = Message(
            id = "msg1",
            cid = "messaging:123",
            parentId = "parent1",
            showInChannel = true,
        )

        // When
        val result = logicRegistry.channelFromMessage(message)

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("messaging:123", result!!.cid)
    }

    @Test
    fun `channelFromMessage should return null for thread-only reply`() {
        // Given
        logicRegistry.channel("messaging", "123")
        val message = Message(
            id = "msg1",
            cid = "messaging:123",
            parentId = "parent1",
            showInChannel = false,
        )

        // When
        val result = logicRegistry.channelFromMessage(message)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `channelFromMessageId should find channel containing the message`() {
        // Given
        logicRegistry.channel("messaging", "123")
        val mockState = channelStateMocks["messaging" to "123"]!!
        val message = Message(id = "msg1", cid = "messaging:123")
        whenever(mockState.getMessageById("msg1")).thenReturn(message)

        // When
        val result = logicRegistry.channelFromMessageId("msg1")

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("messaging:123", result!!.cid)
    }

    @Test
    fun `channelFromMessageId should return null when no channel has the message`() {
        // Given
        logicRegistry.channel("messaging", "123")

        // When
        val result = logicRegistry.channelFromMessageId("nonexistent")

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `getMessageById should find message in channel`() {
        // Given
        logicRegistry.channel("messaging", "123")
        val mockState = channelStateMocks["messaging" to "123"]!!
        val message = Message(id = "msg1", cid = "messaging:123")
        whenever(mockState.getMessageById("msg1")).thenReturn(message)

        // When
        val result = logicRegistry.getMessageById("msg1")

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("msg1", result!!.id)
    }

    @Test
    fun `getMessageById should return null when message not found`() {
        // Given
        logicRegistry.channel("messaging", "123")

        // When
        val result = logicRegistry.getMessageById("nonexistent")

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `clear should remove all channel logic entries`() {
        // Given
        logicRegistry.channel("messaging", "123")
        logicRegistry.channel("messaging", "456")
        Assertions.assertEquals(2, logicRegistry.getActiveChannelsLogic().size)

        // When
        logicRegistry.clear()

        // Then
        Assertions.assertTrue(logicRegistry.getActiveChannelsLogic().isEmpty())
        Assertions.assertFalse(logicRegistry.isActiveChannel("messaging", "123"))
        Assertions.assertFalse(logicRegistry.isActiveChannel("messaging", "456"))
    }

    // endregion
}
