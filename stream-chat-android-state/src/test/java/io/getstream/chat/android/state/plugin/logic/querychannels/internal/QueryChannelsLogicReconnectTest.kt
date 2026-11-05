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
import io.getstream.chat.android.client.api.models.QueryChannelsResult
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** A reconnect refresh of a standard channel list that has loaded more than its first page, on the real state. */
internal class QueryChannelsLogicReconnectTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private val filter = Filters.eq("type", "messaging")
    private val sort = QuerySortByField.descByName<Channel>("last_message_at")
    private val client: ChatClient = mock()
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic = mock {
        onBlocking { selectLocalOnlyMessages(any()) } doReturn emptyList()
    }
    private lateinit var mutableState: QueryChannelsMutableState
    private lateinit var stateLogic: QueryChannelsStateLogic
    private lateinit var logic: QueryChannelsLogic

    @BeforeEach
    fun setUp() {
        val identifier = QueryChannelsIdentifier.Standard(filter, sort)
        mutableState = QueryChannelsMutableState(
            identifier = identifier,
            scope = testCoroutines.scope,
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
        )
        stateLogic = QueryChannelsStateLogic(
            mutableState = mutableState,
            stateRegistry = mock(),
            logicRegistry = mock(defaultAnswer = Answers.RETURNS_MOCKS),
            coroutineScope = testCoroutines.scope,
        )
        logic = QueryChannelsLogic(
            identifier = identifier,
            client = client,
            queryChannelsStateLogic = stateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
    }

    @Test
    fun `a failed later page keeps the loaded channels and the offset`() = runTest {
        val loadedChannels = loadPages(count = 2)
        val newChannel = randomChannel(type = "messaging")
        givenPages(0 to listOf(newChannel) + loadedChannels.take(PAGE_SIZE - 1))

        logic.queryFirstPage()

        val expectedCids = loadedChannels.cids() + newChannel.cid
        assertEquals(expectedCids, stateLogic.getChannels()!!.keys)
        assertEquals(expectedCids, logic.cids())
        assertEquals(PAGE_SIZE * 2, mutableState.channelsOffset.value)
    }

    @Test
    fun `a page failing after a successful later page keeps the loaded channels and the offset`() = runTest {
        val loadedChannels = loadPages(count = 3)
        givenPages(
            0 to loadedChannels.take(PAGE_SIZE),
            PAGE_SIZE to loadedChannels.subList(PAGE_SIZE, PAGE_SIZE * 2),
        )

        logic.queryFirstPage()

        assertEquals(loadedChannels.cids(), stateLogic.getChannels()!!.keys)
        assertEquals(loadedChannels.cids(), logic.cids())
        assertEquals(PAGE_SIZE * 3, mutableState.channelsOffset.value)
    }

    @Test
    fun `a failed later page keeps the offset of a page loaded meanwhile`() = runTest {
        val loadedChannels = loadPages(count = 2)
        whenever(client.queryChannelsInternal(any())) doAnswer { invocation ->
            val request = invocation.getArgument<QueryChannelsRequest>(0)
            if (request.offset == 0) {
                QueryChannelsResult(channels = loadedChannels.take(PAGE_SIZE), predefinedFilter = null).asCall()
            } else {
                // A next page loads while the later pages are re-queried
                stateLogic.incrementChannelsOffset(PAGE_SIZE)
                Error.GenericError("Page at offset ${request.offset} failed").asCall()
            }
        }

        logic.queryFirstPage()

        assertEquals(PAGE_SIZE * 3, mutableState.channelsOffset.value)
    }

    @Test
    fun `a successful later page removes the channel that left the list`() = runTest {
        val loadedChannels = loadPages(count = 2)
        val leftChannel = loadedChannels.last()
        givenPages(
            0 to loadedChannels.take(PAGE_SIZE),
            PAGE_SIZE to loadedChannels.drop(PAGE_SIZE) - leftChannel,
        )

        logic.queryFirstPage()

        assertEquals(loadedChannels.cids() - leftChannel.cid, stateLogic.getChannels()!!.keys)
        assertEquals(loadedChannels.cids() - leftChannel.cid, logic.cids())
        assertEquals(PAGE_SIZE * 2 - 1, mutableState.channelsOffset.value)
    }

    /** Loads [count] full pages into the list, the way the channel list loads them before the connection drops. */
    private suspend fun loadPages(count: Int): List<Channel> {
        val channels = List(PAGE_SIZE * count) { randomChannel(type = "messaging") }
        channels.chunked(PAGE_SIZE).forEachIndexed { index, page ->
            val request = QueryChannelsRequest(
                filter = filter,
                offset = index * PAGE_SIZE,
                limit = PAGE_SIZE,
                querySort = sort,
            )
            logic.onQueryChannelsResult(Result.Success(page), request)
        }
        assertEquals(channels.cids(), stateLogic.getChannels()!!.keys)
        return channels
    }

    /** Stubs the page at each offset of [pages]. A request at any other offset fails. */
    private fun givenPages(vararg pages: Pair<Int, List<Channel>>) {
        val pagesByOffset = pages.toMap()
        whenever(client.queryChannelsInternal(any())) doAnswer { invocation ->
            val request = invocation.getArgument<QueryChannelsRequest>(0)
            pagesByOffset[request.offset]
                ?.let { QueryChannelsResult(channels = it, predefinedFilter = null).asCall() }
                ?: Error.GenericError("Page at offset ${request.offset} failed").asCall()
        }
    }

    private fun List<Channel>.cids(): Set<String> = mapTo(mutableSetOf(), Channel::cid)

    private companion object {
        private const val PAGE_SIZE = 30
    }
}
