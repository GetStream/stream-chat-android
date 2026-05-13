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
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
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
            identifier = QueryChannelsIdentifier.Standard(filter, sort),
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
    fun `queryOffline should fetch channels from cache with correct parameters`() = runTest {
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
            eq(queryChannelsSpec),
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
    fun `queryOffline should add channels and reset loading first page state when cached channels found`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        val cachedChannels = listOf(
            randomChannel(id = "channel1", type = "messaging"),
            randomChannel(id = "channel2", type = "messaging"),
            randomChannel(id = "channel3", type = "messaging"),
        )
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn cachedChannels

        // When
        logic.queryOffline(pagination)

        // Then
        verify(queryChannelsStateLogic).setLoadingFirstPage(true)
        verify(queryChannelsStateLogic).addChannelsState(cachedChannels)
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
        verify(queryChannelsStateLogic, never()).setLoadingMore(any())
    }

    @Test
    fun `queryOffline should add empty list and reset loading when cached empty list is found`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest().apply {
            channelOffset = 0
        }
        val cachedChannels = emptyList<Channel>()
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn cachedChannels

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
        whenever(queryChannelsStateLogic.isLoading()) doReturn false
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn cachedChannels
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

    // region loadOfflineChannels

    @Test
    fun `loadOfflineChannels populates state from cache`() = runTest {
        // Given
        val request = QueryChannelsRequest(filter = filter, limit = 30, querySort = sort)
        val cachedChannels = listOf(randomChannel(), randomChannel())
        whenever(queryChannelsStateLogic.getChannels()) doReturn null
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn cachedChannels

        // When
        logic.loadOfflineChannels(request)

        // Then
        verify(queryChannelsStateLogic).setCurrentRequest(request)
        verify(queryChannelsStateLogic).addChannelsState(cachedChannels)
        verify(queryChannelsStateLogic).initializeChannelsIfNeeded()
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
        verify(queryChannelsStateLogic, never()).setChannelsOffset(any())
    }

    @Test
    fun `loadOfflineChannels handles null cache gracefully`() = runTest {
        // Given
        val request = QueryChannelsRequest(filter = filter, limit = 30, querySort = sort)
        whenever(queryChannelsStateLogic.getChannels()) doReturn null
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn null

        // When
        logic.loadOfflineChannels(request)

        // Then
        verify(queryChannelsStateLogic).setCurrentRequest(request)
        verify(queryChannelsStateLogic, never()).addChannelsState(any())
        verify(queryChannelsStateLogic).initializeChannelsIfNeeded()
        verify(queryChannelsStateLogic).setLoadingFirstPage(false)
    }

    @Test
    fun `loadOfflineChannels skips when channels already populated`() = runTest {
        // Given - race condition: channels were populated by a concurrent prefill
        val request = QueryChannelsRequest(filter = filter, limit = 30, querySort = sort)
        val existingChannels = mapOf("messaging:ch1" to randomChannel())
        whenever(queryChannelsStateLogic.getChannels()) doReturn existingChannels
        whenever(queryChannelsDatabaseLogic.fetchChannelsFromCache(any(), any())) doReturn listOf(randomChannel())

        // When
        logic.loadOfflineChannels(request)

        // Then - only setCurrentRequest should be called, nothing else
        verify(queryChannelsStateLogic).setCurrentRequest(request)
        verify(queryChannelsStateLogic, never()).addChannelsState(any())
        verify(queryChannelsStateLogic, never()).initializeChannelsIfNeeded()
        verify(queryChannelsStateLogic, never()).setLoadingFirstPage(any())
    }

    // endregion

    // region applyGroupedResult

    @Test
    fun `applyGroupedResult is a no-op on non-Grouped identifiers`() = runTest {
        // Given — logic is constructed with a Standard identifier in setUp.
        val channels = listOf(randomChannel(id = "new1"))
        val group = GroupedChannelsGroup(groupKey = "key", channels = channels)

        // When
        logic.applyGroupedResult(group, isFirstPage = true)

        // Then — no state mutations on a non-Grouped logic.
        verify(queryChannelsStateLogic, never()).addChannelsState(any())
        verify(queryChannelsStateLogic, never()).setNextCursor(any())
    }

    // endregion
}
