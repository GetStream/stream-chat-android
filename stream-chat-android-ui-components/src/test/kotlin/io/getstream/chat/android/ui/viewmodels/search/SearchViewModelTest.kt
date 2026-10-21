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

package io.getstream.chat.android.ui.viewmodels.search

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.observeAll
import io.getstream.chat.android.ui.viewmodel.search.SearchViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class SearchViewModelTest {

    @Test
    fun `Given empty query When setting query Should clear results`() = runTest {
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenSearchMessagesResult(SearchMessagesResult(messages = listOf(randomMessage(cid = "messaging:ch1"))))
            .givenRepositorySelectChannels()
            .get()

        viewModel.setQuery("hello")
        viewModel.setQuery("")

        val states = viewModel.state.observeAll()
        val lastState = states.last()
        assertEquals(0, lastState.results.size)
        assertFalse(lastState.isLoading)
        assertFalse(lastState.canLoadMore)
        assertNull(lastState.next)
    }

    @Test
    fun `Given search query When searching Should call searchMessages without offset or cursor`() = runTest {
        val chatClient: ChatClient = mock()
        val messages = listOf(randomMessage(cid = "messaging:ch1"))
        val searchResult = SearchMessagesResult(messages = messages, next = "cursor_page2")
        val viewModel = Fixture(chatClient)
            .givenCurrentUser()
            .givenSearchMessagesResult(searchResult)
            .givenRepositorySelectChannels()
            .get()

        viewModel.setQuery("hello")

        verify(chatClient).searchMessages(
            channelFilter = any(),
            messageFilter = any(),
            offset = eq(null),
            limit = any(),
            next = eq(null),
            sort = eq(null),
        )
    }

    @Test
    fun `Given search results with next cursor When loading more Should pass the cursor`() = runTest {
        val chatClient: ChatClient = mock()
        val firstPageMessages = listOf(randomMessage(cid = "messaging:ch1"))
        val firstPageResult = SearchMessagesResult(messages = firstPageMessages, next = "cursor_page2")
        val secondPageMessages = listOf(randomMessage(cid = "messaging:ch1"))
        val secondPageResult = SearchMessagesResult(messages = secondPageMessages, next = null)

        whenever(
            chatClient.searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()),
        ).doReturn(
            firstPageResult.asCall(),
            secondPageResult.asCall(),
        )

        val viewModel = Fixture(chatClient)
            .givenCurrentUser()
            .givenRepositorySelectChannels()
            .get()

        viewModel.setQuery("hello")
        viewModel.loadMore()

        val captor = argumentCaptor<String>()
        verify(chatClient, times(2)).searchMessages(
            channelFilter = any(),
            messageFilter = any(),
            offset = anyOrNull(),
            limit = anyOrNull(),
            next = captor.capture(),
            sort = anyOrNull(),
        )
        assertNull(captor.firstValue)
        assertEquals("cursor_page2", captor.secondValue)
    }

    @Test
    fun `Given search results without next cursor When loading more Should not load more`() = runTest {
        val chatClient: ChatClient = mock()
        val messages = listOf(randomMessage(cid = "messaging:ch1"))
        val searchResult = SearchMessagesResult(messages = messages, next = null)
        val viewModel = Fixture(chatClient)
            .givenCurrentUser()
            .givenSearchMessagesResult(searchResult)
            .givenRepositorySelectChannels()
            .get()

        viewModel.setQuery("hello")

        val states = viewModel.state.observeAll()
        val lastState = states.last()
        assertFalse(lastState.canLoadMore)

        viewModel.loadMore()

        verify(chatClient, times(1)).searchMessages(
            channelFilter = any(),
            messageFilter = any(),
            offset = anyOrNull(),
            limit = anyOrNull(),
            next = anyOrNull(),
            sort = anyOrNull(),
        )
    }

    @Test
    fun `Given search results When another user connects Should clear the results and search again as that user`() =
        runTest {
            val fixture = givenSearchResultsFixture()
            val viewModel = fixture.get()
            val states = viewModel.state.observeAll()
            viewModel.setQuery("hello")

            fixture.startConnectingUser(User(id = "other"))
            assertEquals(SearchViewModel.State(query = "hello", isLoading = true), states.last())
            fixture.finishConnecting()

            val captor = argumentCaptor<FilterObject>()
            verify(fixture.chatClient, times(2)).searchMessages(
                captor.capture(),
                any(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            )
            assertEquals(Filters.`in`("members", listOf("other")), captor.lastValue)
            assertEquals(1, viewModel.state.value!!.results.size)
        }

    @Test
    fun `Given a user switch When a new query is typed before the user is ready Should keep the new query`() =
        runTest {
            val fixture = givenSearchResultsFixture()
            val viewModel = fixture.get()
            viewModel.state.observeAll()
            viewModel.setQuery("hello")

            fixture.startConnectingUser(User(id = "other"))
            viewModel.setQuery("bye")
            fixture.finishConnecting()

            val captor = argumentCaptor<FilterObject>()
            verify(fixture.chatClient, times(2)).searchMessages(
                any(),
                captor.capture(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            )
            assertEquals(Filters.autocomplete("text", "bye"), captor.lastValue)
            assertEquals("bye", viewModel.state.value!!.query)
        }

    @Test
    fun `Given search results When the user disconnects Should clear the results`() = runTest {
        val fixture = givenSearchResultsFixture()
        val viewModel = fixture.get()
        val states = viewModel.state.observeAll()
        viewModel.setQuery("hello")

        fixture.disconnectUser()

        assertEquals(SearchViewModel.State(query = "hello", isLoading = true), states.last())
    }

    @Test
    fun `Given a search waiting for the next user When loading more Should keep the search`() = runTest {
        val fixture = givenSearchResultsFixture()
        val viewModel = fixture.get()
        viewModel.state.observeAll()
        viewModel.setQuery("hello")

        fixture.startConnectingUser(User(id = "other"))
        viewModel.loadMore()
        fixture.finishConnecting()

        verify(fixture.chatClient, times(2))
            .searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        assertEquals(1, viewModel.state.value!!.results.size)
        assertFalse(viewModel.state.value!!.isLoading)
    }

    @Test
    fun `Given a user still connecting When searching Should wait until they are ready`() = runTest {
        val fixture = givenSearchResultsFixture()
        val viewModel = fixture.get()
        viewModel.state.observeAll()

        fixture.startConnectingUser(User(id = "other"))
        viewModel.setQuery("hello")
        verify(fixture.chatClient, never())
            .searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        fixture.finishConnecting()
        verify(fixture.chatClient).searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `Given no user once the client is ready When searching Should report an error`() = runTest {
        val fixture = givenSearchResultsFixture()
        val viewModel = fixture.get()
        val states = viewModel.state.observeAll()
        val errors = viewModel.errorEvents.observeAll()

        fixture.loseUserOnceReady()
        viewModel.setQuery("hello")

        verify(fixture.chatClient, never())
            .searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        assertFalse(states.last().isLoading)
        assertEquals(1, errors.size)
    }

    private suspend fun givenSearchResultsFixture() = Fixture()
        .givenCurrentUser()
        .givenSearchMessagesResult(SearchMessagesResult(messages = listOf(randomMessage(cid = "messaging:ch1"))))
        .givenRepositorySelectChannels()

    private class Fixture(
        val chatClient: ChatClient = mock(),
    ) {
        private val clientState: ClientState = mock()
        private val repositoryFacade: RepositoryFacade = mock()
        private val user = MutableStateFlow<User?>(null)
        private val initializationState = MutableStateFlow(InitializationState.NOT_INITIALIZED)

        init {
            whenever(chatClient.clientState) doReturn clientState
            whenever(chatClient.repositoryFacade) doReturn repositoryFacade
        }

        fun givenCurrentUser(currentUser: User = User(id = "Jc")) = apply {
            user.value = currentUser
            initializationState.value = InitializationState.COMPLETE
            whenever(clientState.user) doReturn user
            whenever(clientState.initializationState) doReturn initializationState
        }

        fun disconnectUser() {
            initializationState.value = InitializationState.NOT_INITIALIZED
            user.value = null
        }

        fun startConnectingUser(newUser: User) {
            // Same order as ChatClient: disconnect, then connectUser sets the user before the client is ready.
            initializationState.value = InitializationState.NOT_INITIALIZED
            user.value = null
            initializationState.value = InitializationState.INITIALIZING
            user.value = newUser
        }

        fun loseUserOnceReady() {
            user.value = null
            initializationState.value = InitializationState.COMPLETE
        }

        fun finishConnecting() {
            initializationState.value = InitializationState.COMPLETE
        }

        fun givenSearchMessagesResult(result: SearchMessagesResult) = apply {
            whenever(
                chatClient.searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()),
            ) doReturn result.asCall()
        }

        suspend fun givenRepositorySelectChannels(channels: List<Channel> = emptyList()) = apply {
            whenever(repositoryFacade.selectChannels(any<List<String>>())) doReturn channels
        }

        fun get(): SearchViewModel = SearchViewModel(chatClient = chatClient)
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines: TestCoroutineExtension = TestCoroutineExtension()

        @JvmField
        @RegisterExtension
        val instantExecutorExtension: InstantTaskExecutorExtension = InstantTaskExecutorExtension()
    }
}
