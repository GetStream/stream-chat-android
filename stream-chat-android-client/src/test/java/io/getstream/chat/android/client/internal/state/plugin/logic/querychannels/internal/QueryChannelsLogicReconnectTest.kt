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
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QueryChannelsResult
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Answers
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** A reconnect refresh of a standard channel list that has loaded more than its first page, on the real state. */
internal class QueryChannelsLogicReconnectTest {

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
            isLocalUnreadCountEnabled = false,
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
        val loadedChannels = loadTwoPages()
        givenPages(firstPage = loadedChannels.take(PAGE_SIZE), laterPage = null)

        logic.queryFirstPage()

        assertEquals(loadedChannels.cids(), stateLogic.getChannels()!!.keys)
        assertEquals(loadedChannels.cids(), logic.cids())
        assertEquals(PAGE_SIZE * 2, mutableState.channelsOffset.value)
    }

    @Test
    fun `a successful later page removes the channel that left the list`() = runTest {
        val loadedChannels = loadTwoPages()
        val leftChannel = loadedChannels.last()
        givenPages(
            firstPage = loadedChannels.take(PAGE_SIZE),
            laterPage = loadedChannels.drop(PAGE_SIZE) - leftChannel,
        )

        logic.queryFirstPage()

        assertEquals(loadedChannels.cids() - leftChannel.cid, stateLogic.getChannels()!!.keys)
        assertEquals(loadedChannels.cids() - leftChannel.cid, logic.cids())
        assertEquals(PAGE_SIZE * 2 - 1, mutableState.channelsOffset.value)
    }

    /** Loads two full pages into the list, the way the channel list loads them before the connection drops. */
    private suspend fun loadTwoPages(): List<Channel> {
        val channels = List(PAGE_SIZE * 2) { randomChannel(type = "messaging") }
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

    /** Stubs the first page, and every later page with [laterPage], or with a failure when it is null. */
    private fun givenPages(firstPage: List<Channel>, laterPage: List<Channel>?) {
        whenever(client.queryChannelsInternal(any())) doAnswer { invocation ->
            val request = invocation.getArgument<QueryChannelsRequest>(0)
            val page = if (request.offset == 0) firstPage else laterPage
            page?.let { QueryChannelsResult(channels = it, predefinedFilter = null).asCall() }
                ?: Error.GenericError("Later page failed").asCall()
        }
    }

    private fun List<Channel>.cids(): Set<String> = mapTo(mutableSetOf(), Channel::cid)

    private companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val PAGE_SIZE = 30
    }
}
