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

package io.getstream.chat.android.ui.common.feature.threads

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.api.state.QueryThreadsState
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomThread
import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ThreadListControllerTest {

    @Test
    fun `initial state is correct`() = runTest {
        val sut = Fixture().get(backgroundScope)

        sut.state.value.let { state ->
            assertTrue(state.threads.isEmpty())
            assertTrue(state.isLoading)
            assertFalse(state.isLoadingMore)
            assertEquals(0, state.unseenThreadsCount)
        }
    }

    @Test
    fun `initial load is correct`() = runTest {
        val thread1 = randomThread()
        val thread2 = randomThread()
        val expectedThreads = listOf(thread1, thread2)
        val state = mock<QueryThreadsState> {
            on { threads } doReturn MutableStateFlow(expectedThreads)
            on { loading } doReturn MutableStateFlow(false)
            on { loadingMore } doReturn MutableStateFlow(false)
            on { unseenThreadIds } doReturn MutableStateFlow(emptySet())
        }
        val sut = Fixture()
            .givenQueryThreadsState(state)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(expectedThreads, actual.threads)
            assertFalse(actual.isLoading)
            assertFalse(actual.isLoadingMore)
            assertEquals(0, actual.unseenThreadsCount)
        }
    }

    @Test
    fun `load calls queryThreadsResult with correct query`() = runTest {
        val query = QueryThreadsRequest()
        val fixture = Fixture()
        val sut = fixture
            .givenQueryThreadsRequest(query)
            .get(backgroundScope, query)

        sut.load()

        fixture.verifyQueryThreadsResult(query)
    }

    @Test
    fun `loadNextPage does nothing if the state is loading`() = runTest {
        val query = QueryThreadsRequest()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope, query)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.loadNextPage()

            fixture.verifyNeverQueryThreadsResult(query)
        }
    }

    @Test
    fun `loadNextPage does nothing if the state is loadingMore`() = runTest {
        val query = QueryThreadsRequest()
        val state = mock<QueryThreadsState> {
            on { threads } doReturn MutableStateFlow(emptyList())
            on { loading } doReturn MutableStateFlow(false)
            on { loadingMore } doReturn MutableStateFlow(true)
            on { unseenThreadIds } doReturn MutableStateFlow(emptySet())
        }
        val fixture = Fixture().givenQueryThreadsState(state)
        val sut = fixture.get(backgroundScope, query)

        sut.state.test {
            skipItems(1) // Skip initial state

            awaitItem()

            sut.loadNextPage()

            fixture.verifyNeverQueryThreadsResult(query)
        }
    }

    @Test
    fun `loadNextPage does nothing if the next page is null`() = runTest {
        val query = QueryThreadsRequest()
        val state = mock<QueryThreadsState> {
            on { threads } doReturn MutableStateFlow(emptyList())
            on { loading } doReturn MutableStateFlow(false)
            on { loadingMore } doReturn MutableStateFlow(false)
            on { unseenThreadIds } doReturn MutableStateFlow(emptySet())
            on { next } doReturn MutableStateFlow(null)
        }
        val fixture = Fixture().givenQueryThreadsState(state)
        val sut = fixture.get(backgroundScope, query)

        sut.state.test {
            skipItems(1) // Skip initial state

            awaitItem()

            sut.loadNextPage()

            fixture.verifyNeverQueryThreadsResult(query)
        }
    }

    @Test
    fun `loadNextPage calls queryThreadsResult with next page query if shouldLoadNextPage returns true`() = runTest {
        val nextPage = randomString()
        val nextPageQuery = QueryThreadsRequest(next = nextPage)
        val state = mock<QueryThreadsState> {
            on { threads } doReturn MutableStateFlow(emptyList())
            on { loading } doReturn MutableStateFlow(false)
            on { loadingMore } doReturn MutableStateFlow(false)
            on { unseenThreadIds } doReturn MutableStateFlow(emptySet())
            on { next } doReturn MutableStateFlow(nextPage)
        }
        val fixture = Fixture()
            .givenQueryThreadsState(state)
            .givenQueryThreadsRequest(nextPageQuery)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            awaitItem()

            sut.loadNextPage()

            fixture.verifyQueryThreadsResult(nextPageQuery)
        }
    }

    private class Fixture {

        private val queryThreadsStateFlow = MutableStateFlow<QueryThreadsState?>(null)

        private val mockChatClient: ChatClient = mock()

        fun givenQueryThreadsState(state: QueryThreadsState) = apply {
            queryThreadsStateFlow.value = state
        }

        fun givenQueryThreadsRequest(query: QueryThreadsRequest) = apply {
            whenever(mockChatClient.queryThreads(query)) doReturn
                mock<Call<QueryThreadsResult>>()
        }

        fun verifyQueryThreadsResult(query: QueryThreadsRequest) = apply {
            verify(mockChatClient).queryThreads(query)
        }

        fun verifyNeverQueryThreadsResult(query: QueryThreadsRequest) = apply {
            verify(mockChatClient, never()).queryThreads(query)
        }

        fun get(
            scope: CoroutineScope,
            query: QueryThreadsRequest = QueryThreadsRequest(),
        ) = ThreadListController(
            query = query,
            chatClient = mockChatClient,
            scope = scope,
            queryThreadsAsState = { queryThreadsStateFlow },
        )
    }
}
