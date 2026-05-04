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

package io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.event.EventHandlingResult
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryChannelsLogicTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private lateinit var filter: FilterObject
    private lateinit var sort: QuerySortByField<Channel>
    private lateinit var identifier: QueryChannelsIdentifier.Standard
    private lateinit var client: ChatClient
    private lateinit var queryChannelsStateLogic: QueryChannelsStateLogic
    private lateinit var queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic
    private lateinit var queryChannelsState: QueryChannelsState
    private lateinit var queryChannelsSpec: QueryChannelsSpec
    private lateinit var logic: QueryChannelsLogic

    @BeforeEach
    fun setUp() {
        filter = Filters.eq("type", "messaging")
        sort = QuerySortByField.descByName("last_message_at")
        identifier = QueryChannelsIdentifier.Standard(filter, sort)
        client = mock()
        queryChannelsStateLogic = mock()
        queryChannelsDatabaseLogic = mock()
        queryChannelsState = mock()
        queryChannelsSpec = QueryChannelsSpec(filter, sort)

        whenever(queryChannelsStateLogic.getState()) doReturn queryChannelsState
        whenever(queryChannelsState.recoveryNeeded) doReturn MutableStateFlow(false)
        whenever(queryChannelsState.currentRequest) doReturn MutableStateFlow(null)
        whenever(queryChannelsStateLogic.getQuerySpecs()) doReturn queryChannelsSpec

        logic = QueryChannelsLogic(
            identifier = identifier,
            client = client,
            queryChannelsStateLogic = queryChannelsStateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
    }

    @Test
    fun `queryOffline should not execute when another query is already in progress`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest()
        whenever(queryChannelsStateLogic.isLoading()) doReturn true

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).isLoading()
        verify(queryChannelsStateLogic, never()).setLoadingFirstPage(any())
        verify(queryChannelsStateLogic, never()).setLoadingMore(any())
        verify(queryChannelsDatabaseLogic, never()).fetchChannelsFromCache(any(), any())
    }

    @Test
    fun `queryOffline should set loading first page when offset is 0`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn null

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).setLoadingFirstPage(true)
        verify(queryChannelsStateLogic, never()).setLoadingMore(any())
    }

    @Test
    fun `queryOffline should set loading more when offset is greater than 0`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 30
        }
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn null

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).setLoadingMore(true)
        verify(queryChannelsStateLogic, never()).setLoadingFirstPage(any())
    }

    @Test
    fun `queryOffline should fetch channels from cache with the identifier`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
            channelLimit = 30
        }
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn null

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsDatabaseLogic).fetchChannelsFromCache(
            eq(pagination),
            eq<QueryChannelsIdentifier>(identifier),
        )
    }

    @Test
    fun `queryOffline should not reset loading state when cached spec is not found`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn null

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).setLoadingFirstPage(true)
        verify(queryChannelsStateLogic, never()).setLoadingFirstPage(false)
        verify(queryChannelsStateLogic, never()).setLoadingMore(false)
    }

    @Test
    fun `queryOffline should add channels and reset loading on Standard cache hit without applying spec`() = runTest {
        // Given – Standard cache hit; the spec is already known so applyResolvedSpec is skipped.
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        val cachedChannels = listOf(
            randomChannel(id = "channel1", type = "messaging"),
            randomChannel(id = "channel2", type = "messaging"),
            randomChannel(id = "channel3", type = "messaging"),
        )
        val cached = CachedQueryChannels(spec = queryChannelsSpec, channels = cachedChannels)
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn cached

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).setLoadingFirstPage(true)
        verify(queryChannelsStateLogic, never()).applyResolvedSpec(any(), any())
        verify(queryChannelsStateLogic).addChannelsState(cachedChannels)
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
        verify(queryChannelsStateLogic, never()).setLoadingMore(any())
    }

    @Test
    fun `queryOffline should apply resolved spec from cached predefined spec before adding channels`() = runTest {
        // Given – a Predefined-identifier logic with a predefined cached spec
        val predefinedIdentifier = QueryChannelsIdentifier.Predefined(
            name = "my-filter",
            filterValues = mapOf("a" to 1),
            sortValues = null,
        )
        val predefinedLogic = QueryChannelsLogic(
            identifier = predefinedIdentifier,
            client = client,
            queryChannelsStateLogic = queryChannelsStateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
        val resolvedFilter = Filters.eq("type", "messaging")
        val resolvedSort = QuerySortByField.descByName<Channel>("last_message_at")
        val predefinedSpec = QueryChannelsSpec.create(
            filter = resolvedFilter,
            querySort = resolvedSort,
            predefinedFilterName = "my-filter",
            predefinedFilterValues = mapOf("a" to 1),
            predefinedSortValues = null,
        )
        val cached = CachedQueryChannels(spec = predefinedSpec, channels = listOf(randomChannel()))
        val pagination = AnyChannelPaginationRequest().apply { channelOffset = 0 }
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn cached

        // When
        predefinedLogic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).applyResolvedSpec(eq(resolvedFilter), eq(resolvedSort))
        verify(queryChannelsStateLogic).addChannelsState(cached.channels)
    }

    @Test
    fun `queryOffline should add empty list and reset loading when cache hit returns no channels`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        val cached = CachedQueryChannels(spec = queryChannelsSpec, channels = emptyList())
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn cached

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).setLoadingFirstPage(true)
        verify(queryChannelsStateLogic).addChannelsState(emptyList())
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
    }

    @Test
    fun `queryOffline should insert query channels spec after adding channels`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        val cachedChannels = listOf(randomChannel())
        val cached = CachedQueryChannels(spec = queryChannelsSpec, channels = cachedChannels)
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn cached
        whenever(queryChannelsStateLogic.getQuerySpecs()) doReturn queryChannelsSpec

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).addChannelsState(cachedChannels)
        verify(queryChannelsDatabaseLogic).insertQueryChannels(queryChannelsSpec)
    }

    // region queryFirstPage

    @Test
    fun `queryFirstPage uses null messageLimit and memberLimit when no prior request exists`() = runTest {
        // Given - currentRequest is null (default from setUp)
        whenever(client.queryChannelsInternal(any()))
            .thenReturn(emptyList<Channel>().asCall())

        // When
        logic.queryFirstPage()

        // Then
        val expectedRequest = QueryChannelsRequest(
            filter = filter,
            offset = 0,
            limit = 30,
            querySort = sort,
            messageLimit = null,
            memberLimit = null,
        )
        verify(client).queryChannelsInternal(expectedRequest)
    }

    @Test
    fun `queryFirstPage uses messageLimit and memberLimit from prior request`() = runTest {
        // Given
        val priorRequest = QueryChannelsRequest(
            filter = filter,
            offset = 0,
            limit = 30,
            querySort = sort,
            messageLimit = 5,
            memberLimit = 50,
        )
        whenever(queryChannelsState.currentRequest) doReturn MutableStateFlow(priorRequest)
        whenever(client.queryChannelsInternal(any()))
            .thenReturn(emptyList<Channel>().asCall())

        // When
        logic.queryFirstPage()

        // Then
        val expectedRequest = QueryChannelsRequest(
            filter = filter,
            offset = 0,
            limit = 30,
            querySort = sort,
            messageLimit = 5,
            memberLimit = 50,
        )
        verify(client).queryChannelsInternal(expectedRequest)
    }

    @Test
    fun `queryFirstPage rebuilds a predefined-filter request from a Predefined identifier`() = runTest {
        // Given
        val predefinedIdentifier = QueryChannelsIdentifier.Predefined(
            name = "my-predefined",
            filterValues = mapOf("a" to 1),
            sortValues = mapOf("b" to 2),
        )
        val predefinedLogic = QueryChannelsLogic(
            identifier = predefinedIdentifier,
            client = client,
            queryChannelsStateLogic = queryChannelsStateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
        whenever(client.queryChannelsInternal(any()))
            .thenReturn(emptyList<Channel>().asCall())

        // When
        predefinedLogic.queryFirstPage()

        // Then
        val expectedRequest = QueryChannelsRequest(
            offset = 0,
            limit = 30,
            messageLimit = null,
            memberLimit = null,
            predefinedFilter = "my-predefined",
            filterValues = mapOf("a" to 1),
            sortValues = mapOf("b" to 2),
        )
        verify(client).queryChannelsInternal(expectedRequest)
    }

    // endregion

    // region parseChatEventResults

    @Test
    fun `parseChatEventResults should resolve channels from in-memory state and skip DB`() = runTest {
        // Given
        val channel = randomChannel(type = "messaging", id = "ch1")
        val event = randomNewMessageEvent(cid = channel.cid, channelType = "messaging", channelId = "ch1")
        val expectedResult = EventHandlingResult.Skip

        whenever(queryChannelsStateLogic.getActiveChannelState(channel.cid)) doReturn channel
        whenever(queryChannelsStateLogic.handleChatEvent(eq(event), eq(channel))) doReturn expectedResult

        // When
        val results = logic.parseChatEventResults(listOf(event))

        // Then
        verify(queryChannelsDatabaseLogic, never()).selectChannels(any())
        assertEquals(listOf(expectedResult), results)
    }

    @Test
    fun `parseChatEventResults should fall back to DB when channel is not active in memory`() = runTest {
        // Given
        val channel = randomChannel(type = "messaging", id = "ch1")
        val event = randomNewMessageEvent(cid = channel.cid, channelType = "messaging", channelId = "ch1")
        val expectedResult = EventHandlingResult.Skip

        whenever(queryChannelsStateLogic.getActiveChannelState(channel.cid)) doReturn null
        whenever(queryChannelsDatabaseLogic.selectChannels(listOf(channel.cid))) doReturn listOf(channel)
        whenever(queryChannelsStateLogic.handleChatEvent(eq(event), eq(channel))) doReturn expectedResult

        // When
        val results = logic.parseChatEventResults(listOf(event))

        // Then
        verify(queryChannelsDatabaseLogic).selectChannels(listOf(channel.cid))
        assertEquals(listOf(expectedResult), results)
    }

    @Test
    fun `parseChatEventResults should use mixed resolution - memory for active, DB for inactive`() = runTest {
        // Given
        val inMemoryChannel = randomChannel(type = "messaging", id = "active")
        val dbChannel = randomChannel(type = "messaging", id = "inactive")
        val event1 = randomNewMessageEvent(
            cid = inMemoryChannel.cid,
            channelType = "messaging",
            channelId = "active",
        )
        val event2 = randomNewMessageEvent(
            cid = dbChannel.cid,
            channelType = "messaging",
            channelId = "inactive",
        )

        whenever(queryChannelsStateLogic.getActiveChannelState(inMemoryChannel.cid)) doReturn inMemoryChannel
        whenever(queryChannelsStateLogic.getActiveChannelState(dbChannel.cid)) doReturn null
        whenever(queryChannelsDatabaseLogic.selectChannels(listOf(dbChannel.cid))) doReturn listOf(dbChannel)
        whenever(queryChannelsStateLogic.handleChatEvent(any(), any())) doReturn EventHandlingResult.Skip

        // When
        logic.parseChatEventResults(listOf(event1, event2))

        // Then – only the inactive channel should be fetched from DB
        verify(queryChannelsDatabaseLogic).selectChannels(listOf(dbChannel.cid))
        verify(queryChannelsStateLogic).handleChatEvent(event1, inMemoryChannel)
        verify(queryChannelsStateLogic).handleChatEvent(event2, dbChannel)
    }

    // endregion
}
