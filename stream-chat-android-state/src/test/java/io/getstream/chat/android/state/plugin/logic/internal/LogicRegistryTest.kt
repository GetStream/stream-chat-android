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
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querythreads.internal.QueryThreadsMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
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
}
