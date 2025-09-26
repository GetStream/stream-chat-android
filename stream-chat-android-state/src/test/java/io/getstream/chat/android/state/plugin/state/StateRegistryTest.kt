/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.state

import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.state.plugin.config.MessageLimitConfig
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class StateRegistryTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var stateRegistry: StateRegistry
    private lateinit var userStateFlow: StateFlow<User?>
    private lateinit var latestUsers: StateFlow<Map<String, User>>
    private lateinit var activeLiveLocations: StateFlow<List<Location>>
    private lateinit var job: Job
    private lateinit var scope: TestScope
    private lateinit var messageLimitConfig: MessageLimitConfig

    @BeforeEach
    fun setUp() {
        userStateFlow = MutableStateFlow(null)
        latestUsers = MutableStateFlow(emptyMap())
        activeLiveLocations = MutableStateFlow(emptyList())
        job = Job()
        scope = testCoroutines.scope
        messageLimitConfig = MessageLimitConfig()

        stateRegistry = StateRegistry(
            userStateFlow = userStateFlow,
            latestUsers = latestUsers,
            activeLiveLocations = activeLiveLocations,
            job = job,
            now = { System.currentTimeMillis() },
            scope = scope,
            messageLimitConfig = messageLimitConfig,
        )
    }

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
    fun `queryThreads should return same instance for null filter and same sort`() {
        // Given
        val filter: FilterObject? = null
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

    @Test
    fun `queryThreads should return different instances for null vs non-null filter with same sort`() {
        // Given
        val filter1: FilterObject? = null
        val filter2 = Filters.eq("channel_cid", "messaging:123")
        val sort = QuerySortByField.descByName<Thread>("last_message_at")

        // When
        val queryThreadsState1 = stateRegistry.queryThreads(filter1, sort)
        val queryThreadsState2 = stateRegistry.queryThreads(filter2, sort)

        // Then
        assertNotSame(queryThreadsState1, queryThreadsState2)
    }

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
    fun `mutableQueryThreads should return same instance for null filter and same sort`() {
        // Given
        val filter: FilterObject? = null
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
        // queryThreads returns QueryThreadsState which should be the same as QueryThreadsMutableState
        // since queryThreads calls mutableQueryThreads internally
        assertSame(queryThreadsState, mutableState)
    }

    @Test
    fun `default QueryThreadsRequest sort should return same instances`() {
        // Given - Using the default sort from QueryThreadsRequest
        val filter = Filters.eq("channel_cid", "messaging:123")
        val defaultSort = QuerySortByField
            .descByName<Thread>("has_unread")
            .descByName("last_message_at")
            .descByName("parent_message_id")

        // When
        val state1 = stateRegistry.queryThreads(filter, defaultSort)
        val state2 = stateRegistry.queryThreads(filter, defaultSort)

        // Then
        assertSame(state1, state2)
    }
}
