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
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
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
        whenever(queryChannelsStateLogic.getQuerySpecs()) doReturn queryChannelsSpec

        logic = QueryChannelsLogic(
            filter = filter,
            sort = sort,
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
}
