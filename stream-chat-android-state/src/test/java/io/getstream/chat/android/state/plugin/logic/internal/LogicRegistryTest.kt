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

package io.getstream.chat.android.state.plugin.logic.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.state.plugin.state.querythreads.internal.QueryThreadsMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.ConcurrentHashMap

internal class LogicRegistryTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var logicRegistry: LogicRegistry
    private lateinit var stateRegistry: StateRegistry
    private lateinit var clientState: ClientState
    private lateinit var mutableGlobalState: MutableGlobalState
    private lateinit var repos: RepositoryFacade
    private lateinit var client: ChatClient
    private lateinit var coroutineScope: TestScope

    private val queryChannelsStateCache:
        ConcurrentHashMap<QueryChannelsIdentifier, QueryChannelsMutableState> = ConcurrentHashMap()
    private val threadsStateCache:
        ConcurrentHashMap<Pair<FilterObject?, QuerySorter<Thread>>, QueryThreadsMutableState> = ConcurrentHashMap()

    @BeforeEach
    fun setUp() {
        stateRegistry = mock()
        clientState = mock()
        mutableGlobalState = mock()
        repos = mock()
        client = mock()
        coroutineScope = testCoroutines.scope

        // Stub query channels state. LogicRegistry resolves state via the identifier-based
        // overload, so we stub that one. For Standard identifiers we project the filter/sort back
        // out as the initial values for QueryChannelsMutableState.
        queryChannelsStateCache.clear()
        whenever(stateRegistry.queryChannels(any<QueryChannelsIdentifier>())).thenAnswer {
            val identifier = it.getArgument<QueryChannelsIdentifier>(0)
            val (initialFilter, initialSort) = when (identifier) {
                is QueryChannelsIdentifier.Standard -> identifier.filter to identifier.sort
                is QueryChannelsIdentifier.Predefined -> Filters.neutral() to QuerySortByField<Channel>()
            }
            queryChannelsStateCache.getOrPut(identifier) {
                QueryChannelsMutableState(
                    identifier = identifier,
                    initialFilter = initialFilter,
                    initialSort = initialSort,
                    scope = coroutineScope,
                    latestUsers = MutableStateFlow(emptyMap()),
                    activeLiveLocations = MutableStateFlow(emptyList()),
                )
            }
        }

        whenever(stateRegistry.mutableQueryThreads(anyOrNull(), any())).thenAnswer {
            val filter = it.getArgument<FilterObject?>(0)
            val sort = it.getArgument<QuerySortByField<Thread>>(1)
            threadsStateCache.getOrPut(filter to sort) {
                QueryThreadsMutableState(filter, sort)
            }
        }

        logicRegistry = LogicRegistry(
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            userPresence = true,
            repos = repos,
            client = client,
            coroutineScope = coroutineScope,
            now = { System.currentTimeMillis() },
        )
    }

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

    // -- QueryChannels --

    @Test
    fun `queryChannels should return same instance for same identifier`() {
        // Given
        val identifier = QueryChannelsIdentifier.Standard(
            filter = Filters.eq("type", "messaging"),
            sort = QuerySortByField.descByName("last_message_at"),
        )

        // When
        val logic1 = logicRegistry.queryChannels(identifier)
        val logic2 = logicRegistry.queryChannels(identifier)

        // Then
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return different instances for different filters`() {
        // Given
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val identifier1 = QueryChannelsIdentifier.Standard(Filters.eq("type", "messaging"), sort)
        val identifier2 = QueryChannelsIdentifier.Standard(Filters.eq("type", "livestream"), sort)

        // When
        val logic1 = logicRegistry.queryChannels(identifier1)
        val logic2 = logicRegistry.queryChannels(identifier2)

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return different instances for different sorts`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val identifier1 = QueryChannelsIdentifier.Standard(filter, QuerySortByField.descByName("last_message_at"))
        val identifier2 = QueryChannelsIdentifier.Standard(filter, QuerySortByField.descByName("created_at"))

        // When
        val logic1 = logicRegistry.queryChannels(identifier1)
        val logic2 = logicRegistry.queryChannels(identifier2)

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `queryChannels via request should return same instance as direct call with same identifier`() {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val request = QueryChannelsRequest(filter = filter, querySort = sort, limit = 30)
        val identifier = QueryChannelsIdentifier.Standard(filter, sort)

        // When
        val logic1 = logicRegistry.queryChannels(request)
        val logic2 = logicRegistry.queryChannels(identifier)

        // Then
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return different instances for Standard and Predefined identifiers`() {
        // Given – a Predefined identifier and a Standard identifier are never the same query.
        val standard = QueryChannelsIdentifier.Standard(
            filter = Filters.eq("type", "messaging"),
            sort = QuerySortByField.descByName("last_message_at"),
        )
        val predefined = QueryChannelsIdentifier.Predefined(
            name = "my-filter",
            filterValues = mapOf("a" to 1),
            sortValues = null,
        )

        // When
        val logic1 = logicRegistry.queryChannels(standard)
        val logic2 = logicRegistry.queryChannels(predefined)

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return different instances for Predefined identifiers with different filterValues`() {
        // Given
        val identifier1 = QueryChannelsIdentifier.Predefined("p", mapOf("a" to 1), null)
        val identifier2 = QueryChannelsIdentifier.Predefined("p", mapOf("a" to 2), null)

        // When
        val logic1 = logicRegistry.queryChannels(identifier1)
        val logic2 = logicRegistry.queryChannels(identifier2)

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return same instance for same Predefined identifier`() {
        // Given
        val identifier = QueryChannelsIdentifier.Predefined(
            name = "my-filter",
            filterValues = mapOf("a" to 1),
            sortValues = mapOf("b" to 2),
        )

        // When
        val logic1 = logicRegistry.queryChannels(identifier)
        val logic2 = logicRegistry.queryChannels(identifier)

        // Then
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return different instances for Predefined identifiers with different names`() {
        // Given
        val identifier1 = QueryChannelsIdentifier.Predefined("filter-a", null, null)
        val identifier2 = QueryChannelsIdentifier.Predefined("filter-b", null, null)

        // When
        val logic1 = logicRegistry.queryChannels(identifier1)
        val logic2 = logicRegistry.queryChannels(identifier2)

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `queryChannels should return different instances for Predefined identifiers with different sortValues`() {
        // Given
        val identifier1 = QueryChannelsIdentifier.Predefined("p", null, mapOf("b" to 1))
        val identifier2 = QueryChannelsIdentifier.Predefined("p", null, mapOf("b" to 2))

        // When
        val logic1 = logicRegistry.queryChannels(identifier1)
        val logic2 = logicRegistry.queryChannels(identifier2)

        // Then
        Assertions.assertNotSame(logic1, logic2)
    }

    @Test
    fun `queryChannels via predefined request should return same instance as direct call with matching identifier`() {
        // Given
        val request = QueryChannelsRequest(
            limit = 30,
            predefinedFilter = "my-filter",
            filterValues = mapOf("a" to 1),
            sortValues = mapOf("b" to 2),
        )
        val identifier = QueryChannelsIdentifier.Predefined(
            name = "my-filter",
            filterValues = mapOf("a" to 1),
            sortValues = mapOf("b" to 2),
        )

        // When
        val logic1 = logicRegistry.queryChannels(request)
        val logic2 = logicRegistry.queryChannels(identifier)

        // Then
        Assertions.assertSame(logic1, logic2)
    }

    @Test
    fun `getActiveQueryChannelsLogic should return all created query channels`() {
        // Given
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val identifier1 = QueryChannelsIdentifier.Standard(Filters.eq("type", "messaging"), sort)
        val identifier2 = QueryChannelsIdentifier.Standard(Filters.eq("type", "livestream"), sort)
        val logic1 = logicRegistry.queryChannels(identifier1)
        val logic2 = logicRegistry.queryChannels(identifier2)

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
        val identifier = QueryChannelsIdentifier.Standard(
            filter = Filters.eq("type", "messaging"),
            sort = QuerySortByField.descByName("last_message_at"),
        )
        logicRegistry.queryChannels(identifier)
        Assertions.assertEquals(1, logicRegistry.getActiveQueryChannelsLogic().size)

        // When
        logicRegistry.clear()

        // Then
        Assertions.assertEquals(0, logicRegistry.getActiveQueryChannelsLogic().size)
    }
}
