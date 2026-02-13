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

import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.test.randomQueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelConfig
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryChannelsDatabaseLogicTest {

    private val queryChannelsRepository: QueryChannelsRepository = mock()
    private val channelConfigRepository: ChannelConfigRepository = mock()
    private val channelRepository: ChannelRepository = mock()
    private val repositoryFacade: RepositoryFacade = mock()

    private val logic = QueryChannelsDatabaseLogic(
        queryChannelsRepository = queryChannelsRepository,
        channelConfigRepository = channelConfigRepository,
        channelRepository = channelRepository,
        repositoryFacade = repositoryFacade,
    )

    @Test
    fun `storeStateForChannels should delegate to repository facade`() = runTest {
        // Given
        val channels = listOf(
            randomChannel(),
            randomChannel(),
            randomChannel(),
        )

        // When
        logic.storeStateForChannels(channels)

        // Then
        verify(repositoryFacade).storeStateForChannels(channels)
    }

    @Test
    fun `fetchChannelsFromCache should return null when queryChannelsSpec is null`() = runTest {
        // Given
        val pagination = AnyChannelPaginationRequest()
        val queryChannelsSpec: QueryChannelsSpec? = null

        // When
        val result = logic.fetchChannelsFromCache(pagination, queryChannelsSpec)

        // Then
        assertNull(result)
    }

    @Test
    fun `fetchChannelsFromCache should return null when spec not found in database`() = runTest {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val pagination = AnyChannelPaginationRequest()
        val queryChannelsSpec = randomQueryChannelsSpec(filter = filter, sort = sort)

        whenever(queryChannelsRepository.selectBy(filter, sort)) doReturn null

        // When
        val result = logic.fetchChannelsFromCache(pagination, queryChannelsSpec)

        // Then
        assertNull(result)
        verify(queryChannelsRepository).selectBy(filter, sort)
    }

    @Test
    fun `fetchChannelsFromCache should return channels when spec found in database`() = runTest {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val pagination = AnyChannelPaginationRequest().apply {
            channelLimit = 10
            channelOffset = 0
        }

        val cid1 = "messaging:channel1"
        val cid2 = "messaging:channel2"
        val cid3 = "messaging:channel3"

        val cachedSpec = randomQueryChannelsSpec(
            filter = filter,
            sort = sort,
            cids = setOf(cid1, cid2, cid3),
        )
        val queryChannelsSpec = randomQueryChannelsSpec(filter = filter, sort = sort)

        val channel1 = randomChannel(id = "channel1", type = "messaging")
        val channel2 = randomChannel(id = "channel2", type = "messaging")
        val channel3 = randomChannel(id = "channel3", type = "messaging")
        val expectedChannels = listOf(channel1, channel2, channel3)

        whenever(queryChannelsRepository.selectBy(filter, sort)) doReturn cachedSpec
        whenever(repositoryFacade.selectChannels(listOf(cid1, cid2, cid3), pagination)) doReturn expectedChannels

        // When
        val result = logic.fetchChannelsFromCache(pagination, queryChannelsSpec)

        // Then
        assertEquals(expectedChannels, result)
        verify(queryChannelsRepository).selectBy(filter, sort)
        verify(repositoryFacade).selectChannels(listOf(cid1, cid2, cid3), pagination)
    }

    @Test
    fun `fetchChannelsFromCache should return empty list when spec found but no channels`() = runTest {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val pagination = AnyChannelPaginationRequest()

        val cachedSpec = randomQueryChannelsSpec(
            filter = filter,
            sort = sort,
            cids = emptySet(),
        )
        val queryChannelsSpec = randomQueryChannelsSpec(filter = filter, sort = sort)

        whenever(queryChannelsRepository.selectBy(filter, sort)) doReturn cachedSpec
        whenever(repositoryFacade.selectChannels(emptyList(), pagination)) doReturn emptyList()

        // When
        val result = logic.fetchChannelsFromCache(pagination, queryChannelsSpec)

        // Then
        assertEquals(emptyList<Channel>(), result)
        verify(queryChannelsRepository).selectBy(filter, sort)
        verify(repositoryFacade).selectChannels(emptyList(), pagination)
    }

    @Test
    fun `selectChannel should return channel from repository`() = runTest {
        // Given
        val cid = "messaging:channel123"
        val expectedChannel = randomChannel(id = "channel123", type = "messaging")

        whenever(channelRepository.selectChannel(cid)) doReturn expectedChannel

        // When
        val result = logic.selectChannel(cid)

        // Then
        assertEquals(expectedChannel, result)
        verify(channelRepository).selectChannel(cid)
    }

    @Test
    fun `selectChannels should return list of channels from repository`() = runTest {
        // Given
        val cids = listOf(
            "messaging:channel1",
            "messaging:channel2",
            "messaging:channel3",
        )
        val expectedChannels = listOf(
            randomChannel(id = "channel1", type = "messaging"),
            randomChannel(id = "channel2", type = "messaging"),
            randomChannel(id = "channel3", type = "messaging"),
        )

        whenever(channelRepository.selectChannels(cids)) doReturn expectedChannels

        // When
        val result = logic.selectChannels(cids)

        // Then
        assertEquals(expectedChannels, result)
        verify(channelRepository).selectChannels(cids)
    }

    @Test
    fun `insertQueryChannels should delegate to repository`() = runTest {
        // Given
        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("last_message_at")
        val queryChannelsSpec = randomQueryChannelsSpec(filter = filter, sort = sort)

        // When
        logic.insertQueryChannels(queryChannelsSpec)

        // Then
        verify(queryChannelsRepository).insertQueryChannels(queryChannelsSpec)
    }

    @Test
    fun `insertChannelConfigs should delegate to repository`() = runTest {
        // Given
        val configs = listOf(
            randomChannelConfig(type = randomString()),
            randomChannelConfig(type = randomString()),
            randomChannelConfig(type = randomString()),
        )

        // When
        logic.insertChannelConfigs(configs)

        // Then
        verify(channelConfigRepository).insertChannelConfigs(configs)
    }
}
