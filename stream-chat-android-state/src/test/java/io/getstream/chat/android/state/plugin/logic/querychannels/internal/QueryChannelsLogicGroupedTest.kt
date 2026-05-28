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

package io.getstream.chat.android.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryChannelsLogicGroupedTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private lateinit var client: ChatClient
    private lateinit var queryChannelsStateLogic: QueryChannelsStateLogic
    private lateinit var queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic
    private lateinit var queryChannelsState: QueryChannelsState
    private lateinit var queryChannelsSpec: QueryChannelsSpec
    private lateinit var logic: QueryChannelsLogic

    @BeforeEach
    fun setUp() {
        client = mock()
        queryChannelsStateLogic = mock()
        queryChannelsDatabaseLogic = mock()
        queryChannelsState = mock()
        queryChannelsSpec = QueryChannelsSpec(
            filter = Filters.neutral(),
            querySort = QuerySortByField.descByName<Channel>("last_updated"),
            groupKey = GROUP_KEY,
        )

        whenever(queryChannelsStateLogic.getState()) doReturn queryChannelsState
        whenever(queryChannelsState.recoveryNeeded) doReturn MutableStateFlow(false)
        whenever(queryChannelsState.currentRequest) doReturn MutableStateFlow(null)
        whenever(queryChannelsStateLogic.getQuerySpecs()) doReturn queryChannelsSpec

        logic = QueryChannelsLogic(
            identifier = QueryChannelsIdentifier.Grouped(GROUP_KEY),
            client = client,
            queryChannelsStateLogic = queryChannelsStateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
    }

    // region applyGroupedResult

    @Test
    fun `applyGroupedResult on first page when state is empty adds channels without removing`() = runTest {
        // Given
        val channels = listOf(randomChannel(id = "ch1"), randomChannel(id = "ch2"))
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = channels,
            next = null,
            prev = null,
        )
        whenever(queryChannelsStateLogic.getChannels()) doReturn null

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then
        verify(queryChannelsStateLogic, never()).removeChannels(any())
        verify(queryChannelsStateLogic).setCids(emptySet())
        verify(queryChannelsStateLogic).addChannelsState(channels)
    }

    @Test
    fun `applyGroupedResult on first page when state has existing channels replaces them`() = runTest {
        // Given
        val existing = mapOf("messaging:old1" to randomChannel(id = "old1"))
        val newChannels = listOf(randomChannel(id = "new1"))
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = newChannels,
            next = null,
            prev = null,
        )
        whenever(queryChannelsStateLogic.getChannels()) doReturn existing

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then
        verify(queryChannelsStateLogic).removeChannels(existing.keys)
        verify(queryChannelsStateLogic).setCids(emptySet())
        verify(queryChannelsStateLogic).addChannelsState(newChannels)
    }

    @Test
    fun `applyGroupedResult on subsequent page appends without removing existing`() = runTest {
        // Given
        val existing = mapOf("messaging:old1" to randomChannel(id = "old1"))
        val newChannels = listOf(randomChannel(id = "new1"))
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = newChannels,
            next = "cursor-next",
            prev = null,
        )
        whenever(queryChannelsStateLogic.getChannels()) doReturn existing

        // When
        logic.applyGroupedResult(group, isFirstPage = false)

        // Then — neither removeChannels nor setCids(emptySet()) should fire on a paginated page.
        verify(queryChannelsStateLogic, never()).removeChannels(any())
        verify(queryChannelsStateLogic, never()).setCids(any())
        verify(queryChannelsStateLogic, never()).setChannelsOffset(any())
        verify(queryChannelsStateLogic).addChannelsState(newChannels)
    }

    @Test
    fun `applyGroupedResult on first page resets channelsOffset`() = runTest {
        // Given
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = emptyList(),
            next = null,
            prev = null,
        )

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then — defensive reset so a Standard offset paginator can't pick up stale state.
        verify(queryChannelsStateLogic).setChannelsOffset(0)
    }

    @Test
    fun `applyGroupedResult stores group next cursor`() = runTest {
        // Given
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = emptyList(),
            next = "cursor-xyz",
            prev = null,
        )

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then
        verify(queryChannelsStateLogic).setNextCursor("cursor-xyz")
    }

    @Test
    fun `applyGroupedResult sets endOfChannels to true when next cursor is null`() = runTest {
        // Given
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = emptyList(),
            next = null,
            prev = null,
        )

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then
        verify(queryChannelsStateLogic).setEndOfChannels(true)
    }

    @Test
    fun `applyGroupedResult sets endOfChannels to false when next cursor is non-null`() = runTest {
        // Given
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = emptyList(),
            next = "cursor-next",
            prev = null,
        )

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then
        verify(queryChannelsStateLogic).setEndOfChannels(false)
    }

    @Test
    fun `applyGroupedResult resets loading and recovery flags`() = runTest {
        // Given
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = emptyList(),
            next = null,
            prev = null,
        )

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
        verify(queryChannelsStateLogic).setLoadingMore(false)
        verify(queryChannelsStateLogic).setRecoveryNeeded(false)
    }

    @Test
    fun `applyGroupedResult persists spec, configs, and channels to database`() = runTest {
        // Given
        val channels = listOf(randomChannel(id = "ch1"), randomChannel(id = "ch2"))
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = channels,
            next = null,
            prev = null,
        )

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then
        verify(queryChannelsDatabaseLogic).insertQueryChannels(queryChannelsSpec)
        val expectedConfigs = channels.map { ChannelConfig(it.type, it.config) }
        verify(queryChannelsDatabaseLogic).insertChannelConfigs(expectedConfigs)
        verify(queryChannelsDatabaseLogic).storeStateForChannels(channels.toSet())
    }

    @Test
    fun `applyGroupedResult is a no-op on non-Grouped identifiers`() = runTest {
        // Given — a logic constructed with a Standard identifier.
        val standardLogic = QueryChannelsLogic(
            identifier = QueryChannelsIdentifier.Standard(
                filter = Filters.eq("type", "messaging"),
                sort = QuerySortByField.descByName<Channel>("last_updated"),
            ),
            client = client,
            queryChannelsStateLogic = queryChannelsStateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
        val group = GroupedChannelsGroup(
            groupKey = GROUP_KEY,
            channels = listOf(randomChannel()),
        )

        // When
        standardLogic.applyGroupedResult(group, isFirstPage = true)

        // Then — no state mutations on a non-Grouped logic.
        verify(queryChannelsStateLogic, never()).addChannelsState(any())
        verify(queryChannelsStateLogic, never()).setNextCursor(any())
        verify(queryChannelsStateLogic, never()).setEndOfChannels(any())
        verify(queryChannelsDatabaseLogic, never()).insertQueryChannels(any())
    }

    // endregion

    // region loadOfflineGroupedChannels

    @Test
    fun `loadOfflineGroupedChannels populates state from cache when state is empty`() = runTest {
        // Given
        val cachedChannels = listOf(randomChannel(id = "ch1"), randomChannel(id = "ch2"))
        whenever(queryChannelsStateLogic.getChannels()) doReturn null
        whenever(
            queryChannelsDatabaseLogic.fetchChannelsFromCache(
                any<AnyChannelPaginationRequest>(),
                any<QueryChannelsIdentifier>(),
            ),
        ) doReturn CachedQueryChannels(spec = queryChannelsSpec, channels = cachedChannels)

        // When
        logic.loadOfflineGroupedChannels()

        // Then
        verify(queryChannelsStateLogic).addChannelsState(cachedChannels)
        verify(queryChannelsStateLogic).initializeChannelsIfNeeded()
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
    }

    @Test
    fun `loadOfflineGroupedChannels skips state population when a concurrent apply already populated state`() = runTest {
        // Given — applyGroupedResult landed during the DB read and populated the state first.
        val existingChannels = mapOf("messaging:ch1" to randomChannel(id = "ch1"))
        val cachedChannels = listOf(randomChannel(id = "stale1"))
        whenever(queryChannelsStateLogic.getChannels()) doReturn existingChannels
        whenever(
            queryChannelsDatabaseLogic.fetchChannelsFromCache(
                any<AnyChannelPaginationRequest>(),
                any<QueryChannelsIdentifier>(),
            ),
        ) doReturn CachedQueryChannels(spec = queryChannelsSpec, channels = cachedChannels)

        // When
        logic.loadOfflineGroupedChannels()

        // Then — stale cache must NOT overwrite fresh data, but housekeeping setters still fire.
        verify(queryChannelsStateLogic, never()).addChannelsState(any())
        verify(queryChannelsStateLogic).initializeChannelsIfNeeded()
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
    }

    @Test
    fun `loadOfflineGroupedChannels handles null cache gracefully`() = runTest {
        // Given — no spec found in DB (first time the group is queried on this device).
        whenever(queryChannelsStateLogic.getChannels()) doReturn null
        whenever(
            queryChannelsDatabaseLogic.fetchChannelsFromCache(
                any<AnyChannelPaginationRequest>(),
                any<QueryChannelsIdentifier>(),
            ),
        ) doReturn null

        // When
        logic.loadOfflineGroupedChannels()

        // Then — no channels to add, but state is initialized and loading flag reset.
        verify(queryChannelsStateLogic, never()).addChannelsState(any())
        verify(queryChannelsStateLogic).initializeChannelsIfNeeded()
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
    }

    @Test
    fun `loadOfflineGroupedChannels is a no-op on non-Grouped identifiers`() = runTest {
        // Given — a logic constructed with a Standard identifier.
        val standardLogic = QueryChannelsLogic(
            identifier = QueryChannelsIdentifier.Standard(
                filter = Filters.eq("type", "messaging"),
                sort = QuerySortByField.descByName<Channel>("last_updated"),
            ),
            client = client,
            queryChannelsStateLogic = queryChannelsStateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )

        // When
        standardLogic.loadOfflineGroupedChannels()

        // Then — Standard path's offline read goes through loadOfflineChannels(request), not this.
        verify(queryChannelsDatabaseLogic, never()).fetchChannelsFromCache(any(), any())
        verify(queryChannelsStateLogic, never()).addChannelsState(any())
        verify(queryChannelsStateLogic, never()).initializeChannelsIfNeeded()
        verify(queryChannelsStateLogic, never()).setLoadingFirstPage(any())
    }

    // endregion

    private companion object {
        private const val GROUP_KEY = "test-group"
    }
}
