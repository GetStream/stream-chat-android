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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
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

    private class Fixture(
        private val chatClient: ChatClient = mock(),
    ) {
        private val clientState: ClientState = mock()
        private val repositoryFacade: RepositoryFacade = mock()

        init {
            whenever(chatClient.clientState) doReturn clientState
            whenever(chatClient.repositoryFacade) doReturn repositoryFacade
        }

        fun givenCurrentUser(currentUser: User = User(id = "Jc")) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
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
