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
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** A first page applied by the reconnect refresh, which keeps the pages the group has loaded. */
internal class QueryChannelsLogicGroupedReconnectTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private val client: ChatClient = mock()
    private val queryChannelsStateLogic: QueryChannelsStateLogic = mock()
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic = mock {
        onBlocking { selectLocalOnlyMessages(any()) } doReturn emptyList()
    }
    private lateinit var logic: QueryChannelsLogic

    @BeforeEach
    fun setUp() {
        val queryChannelsState: QueryChannelsState = mock()
        whenever(queryChannelsStateLogic.getState()) doReturn queryChannelsState
        whenever(queryChannelsState.recoveryNeeded) doReturn MutableStateFlow(false)
        whenever(queryChannelsState.currentRequest) doReturn MutableStateFlow(null)
        logic = QueryChannelsLogic(
            identifier = QueryChannelsIdentifier.Grouped(GROUP_KEY),
            client = client,
            queryChannelsStateLogic = queryChannelsStateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
    }

    @Test
    fun `applyGroupedResult keeping loaded pages keeps the channels beyond the first page`() = runTest {
        // Given - more channels loaded than the page, and more pages on the server
        val existing = (1..3).associate { "messaging:old$it" to randomChannel(type = "messaging", id = "old$it") }
        val newChannels = listOf(randomChannel(type = "messaging", id = "new1"))
        val group = GroupedChannelsGroup(groupKey = GROUP_KEY, channels = newChannels, next = "cursor-2", prev = null)
        whenever(queryChannelsStateLogic.getChannels()) doReturn existing

        // When
        logic.applyGroupedResult(group, isFirstPage = true, keepsLoadedPages = true)

        // Then
        verify(queryChannelsStateLogic, never()).removeChannels(any())
        verify(queryChannelsStateLogic, never()).setCids(any())
        verify(queryChannelsStateLogic).setChannelsOffset(0)
        verify(queryChannelsStateLogic).setNextCursor("cursor-2")
        verify(queryChannelsStateLogic).setEndOfChannels(false)
        verify(queryChannelsStateLogic).addChannelsState(newChannels)
    }

    @Test
    fun `applyGroupedResult keeping loaded pages resets a group that has no more pages`() = runTest {
        // Given - the page is the whole group
        val existing = (1..3).associate { "messaging:old$it" to randomChannel(type = "messaging", id = "old$it") }
        val newChannels = listOf(randomChannel(type = "messaging", id = "new1"))
        val group = GroupedChannelsGroup(groupKey = GROUP_KEY, channels = newChannels, next = null, prev = null)
        whenever(queryChannelsStateLogic.getChannels()) doReturn existing

        // When
        logic.applyGroupedResult(group, isFirstPage = true, keepsLoadedPages = true)

        // Then
        verify(queryChannelsStateLogic).removeChannels(existing.keys)
        verify(queryChannelsStateLogic).setCids(emptySet())
    }

    @Test
    fun `applyGroupedResult keeping loaded pages resets a group that loaded no more than the page`() = runTest {
        // Given - only the first page was loaded
        val existing = mapOf("messaging:old1" to randomChannel(type = "messaging", id = "old1"))
        val newChannels = listOf(randomChannel(type = "messaging", id = "new1"))
        val group = GroupedChannelsGroup(groupKey = GROUP_KEY, channels = newChannels, next = "cursor-2", prev = null)
        whenever(queryChannelsStateLogic.getChannels()) doReturn existing

        // When
        logic.applyGroupedResult(group, isFirstPage = true, keepsLoadedPages = true)

        // Then
        verify(queryChannelsStateLogic).removeChannels(setOf("messaging:old1"))
        verify(queryChannelsStateLogic).setCids(emptySet())
    }

    @Test
    fun `a reconnect first page keeps the second page that was loaded and pages on from the new cursor`() = runTest {
        // Given - real state with two pages loaded
        val mutableState = QueryChannelsMutableState(
            identifier = QueryChannelsIdentifier.Grouped(GROUP_KEY),
            scope = testCoroutines.scope,
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
        )
        val stateLogic = QueryChannelsStateLogic(
            mutableState = mutableState,
            stateRegistry = mock(),
            logicRegistry = mock { on(it.channelState(any(), any())) doReturn mock() },
            coroutineScope = testCoroutines.scope,
        )
        val realLogic = QueryChannelsLogic(
            identifier = QueryChannelsIdentifier.Grouped(GROUP_KEY),
            client = client,
            queryChannelsStateLogic = stateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
        val firstPage = listOf(randomChannel(type = "messaging", id = "a"), randomChannel(type = "messaging", id = "b"))
        val secondPage = listOf(randomChannel(type = "messaging", id = "c"), randomChannel(type = "messaging", id = "d"))
        realLogic.applyGroupedResult(group(firstPage, next = "cursor-2"), isFirstPage = true)
        realLogic.applyGroupedResult(group(secondPage, next = "cursor-3"), isFirstPage = false)
        val newChannel = randomChannel(type = "messaging", id = "new")

        // When - the refreshed first page has a channel added while offline, which pushes "b" to the next page
        realLogic.applyGroupedResult(
            group(listOf(newChannel, firstPage[0]), next = "cursor-2b"),
            isFirstPage = true,
            keepsLoadedPages = true,
        )

        // Then
        assertEquals(setOf("new", "a", "b", "c", "d"), stateLogic.getChannels()!!.values.map { it.id }.toSet())
        assertEquals(setOf("new", "a", "b", "c", "d"), stateLogic.getQuerySpecs().cids.map { it.substringAfter(':') }.toSet())
        assertEquals("cursor-2b", mutableState.nextCursor.value)
        assertEquals(false, mutableState.endOfChannels.value)
    }

    private fun group(channels: List<Channel>, next: String?) =
        GroupedChannelsGroup(groupKey = GROUP_KEY, channels = channels, next = next, prev = null)

    private companion object {
        private const val GROUP_KEY = "test-group"
    }
}
