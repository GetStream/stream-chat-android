/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.mentions

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.test.MockRetrofitCall
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.mentions.MentionListEvent
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class MentionListControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture().get(backgroundScope)

        sut.state.value.let { state ->
            assertTrue(state.isLoading)
            assertTrue(state.results.isEmpty())
            assertNull(state.nextPage)
            assertTrue(state.canLoadMore)
            assertFalse(state.isLoadingMore)
        }
    }

    @Test
    fun `initial load`() = runTest {
        val channel = Channel(id = CHANNEL_ID, type = CHANNEL_TYPE)
        val message1 = Message(text = "text1", cid = CID)
        val message2 = Message(text = "text2", cid = CID)
        val searchMessagesResult = SearchMessagesResult(
            messages = listOf(message1, message2),
            next = "next",
        )
        val sut = Fixture()
            .givenCurrentUser()
            .givenChannels(channels = listOf(channel))
            .givenSearchMessagesResult(next = null, result = searchMessagesResult)
            .get(backgroundScope)

        val expectedResults = listOf(
            MessageResult(message = message1, channel = channel),
            MessageResult(message = message2, channel = channel),
        )
        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertFalse(actual.isLoading)
            assertEquals(expectedResults, actual.results)
            assertEquals("next", actual.nextPage)
            assertTrue(actual.canLoadMore)
            assertFalse(actual.isLoadingMore)
        }
    }

    @Test
    fun `initial load error`() = runTest {
        val error = Error.GenericError("error")
        val sut = Fixture()
            .givenCurrentUser()
            .givenSearchMessagesResult(next = null, error = error)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertFalse(actual.isLoading)
            assertTrue(actual.results.isEmpty())
            assertNull(actual.nextPage)
            assertTrue(actual.canLoadMore)
            assertFalse(actual.isLoadingMore)
        }
    }

    @Test
    fun `load more`() = runTest {
        val channel = Channel(id = CHANNEL_ID, type = CHANNEL_TYPE)
        val message1 = Message(text = "text1", cid = CID)
        val message2 = Message(text = "text2", cid = CID)
        val firstSearchMessagesResult = SearchMessagesResult(
            messages = listOf(message1, message2),
            next = "next",
        )
        val message3 = Message(text = "text3", cid = CID)
        val message4 = Message(text = "text4", cid = CID)
        val secondSearchMessagesResult = SearchMessagesResult(
            messages = listOf(message3, message4),
            next = null,
        )
        val sut = Fixture()
            .givenCurrentUser()
            .givenChannels(channels = listOf(channel))
            .givenSearchMessagesResult(next = null, result = firstSearchMessagesResult)
            .givenSearchMessagesResult(next = "next", result = secondSearchMessagesResult)
            .get(backgroundScope)

        val expectedFirstPageResults = listOf(
            MessageResult(message = message1, channel = channel),
            MessageResult(message = message2, channel = channel),
        )
        sut.state.test {
            skipItems(1) // Skip initial state
            val initialActual = awaitItem()
            assertFalse(initialActual.isLoading)
            assertEquals(expectedFirstPageResults, initialActual.results)
            assertEquals("next", initialActual.nextPage)
            assertTrue(initialActual.canLoadMore)
            assertFalse(initialActual.isLoadingMore)

            sut.loadMore()
            assertTrue(awaitItem().isLoadingMore)

            val expectedAccumulatedResults = expectedFirstPageResults + listOf(
                MessageResult(message = message3, channel = channel),
                MessageResult(message = message4, channel = channel),
            )
            val finalActual = awaitItem()
            assertFalse(finalActual.isLoading)
            assertEquals(expectedAccumulatedResults, finalActual.results)
            assertNull(finalActual.nextPage)
            assertFalse(finalActual.canLoadMore)
            assertFalse(finalActual.isLoadingMore)
        }
    }

    @Test
    fun `load more error`() = runTest {
        val channel = Channel(id = CHANNEL_ID, type = CHANNEL_TYPE)
        val message1 = Message(text = "text1", cid = CID)
        val message2 = Message(text = "text2", cid = CID)
        val firstSearchMessagesResult = SearchMessagesResult(
            messages = listOf(message1, message2),
            next = "next",
        )
        val searchingError = Error.GenericError("error")
        val sut = Fixture()
            .givenCurrentUser()
            .givenChannels(channels = listOf(channel))
            .givenSearchMessagesResult(next = null, result = firstSearchMessagesResult)
            .givenSearchMessagesResult(next = "next", error = searchingError)
            .get(backgroundScope)

        val expectedResults = listOf(
            MessageResult(message = message1, channel = channel),
            MessageResult(message = message2, channel = channel),
        )
        sut.state.test {
            skipItems(1) // Skip initial state
            val initialActual = awaitItem()
            assertFalse(initialActual.isLoading)
            assertEquals(expectedResults, initialActual.results)
            assertEquals("next", initialActual.nextPage)
            assertTrue(initialActual.canLoadMore)
            assertFalse(initialActual.isLoadingMore)

            sut.loadMore()
            assertTrue(awaitItem().isLoadingMore)

            sut.events.test {
                val event = awaitItem()
                assertInstanceOf<MentionListEvent.Error>(event)
                assertEquals(searchingError.message, event.message)
            }

            val finalActual = awaitItem()
            assertFalse(finalActual.isLoading)
            assertEquals(expectedResults, finalActual.results)
            assertEquals("next", finalActual.nextPage)
            assertTrue(finalActual.canLoadMore)
            assertFalse(finalActual.isLoadingMore)
        }
    }

    @Test
    fun `no more results to load`() = runTest {
        val sut = Fixture()
            .givenCurrentUser()
            .givenSearchMessagesResult(next = null, result = SearchMessagesResult())
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertFalse(actual.isLoading)
            assertTrue(actual.results.isEmpty())
            assertNull(actual.nextPage)
            assertFalse(actual.canLoadMore)
            assertFalse(actual.isLoadingMore)

            sut.loadMore()

            expectNoEvents()
        }
    }

    @Test
    fun `already loading more`() = runTest {
        val sut = Fixture()
            .givenCurrentUser()
            .givenSearchMessagesResult(next = null, result = SearchMessagesResult(next = "next"))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertFalse(actual.isLoading)
            assertTrue(actual.results.isEmpty())
            assertEquals("next", actual.nextPage)
            assertTrue(actual.canLoadMore)
            assertFalse(actual.isLoadingMore)

            sut.loadMore()
            assertTrue(awaitItem().isLoadingMore)

            sut.loadMore()
            expectNoEvents()
        }
    }

    @Test
    fun refresh() = runTest {
        val sut = Fixture().get(backgroundScope)

        sut.refresh()

        sut.state.test {
            val actual = awaitItem()
            assertTrue(actual.isLoading)
            assertTrue(actual.results.isEmpty())
            assertNull(actual.nextPage)
            assertTrue(actual.canLoadMore)
            assertFalse(actual.isLoadingMore)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `refresh right after load more`() = runTest {
        val channel = Channel(id = CHANNEL_ID, type = CHANNEL_TYPE)
        val message1 = Message(text = "text1", cid = CID)
        val message2 = Message(text = "text2", cid = CID)
        val firstSearchMessagesResult = SearchMessagesResult(
            messages = listOf(message1, message2),
            next = "next",
        )
        val searchScope = TestScope(StandardTestDispatcher())
        val sut = Fixture()
            .givenCurrentUser()
            .givenChannels(channels = listOf(channel))
            .givenSearchMessagesResult(scope = searchScope, next = null, result = firstSearchMessagesResult)
            .givenSearchMessagesResult(scope = searchScope, next = "next", result = SearchMessagesResult())
            .get(scope = TestScope(UnconfinedTestDispatcher()))

        val expectedFirstPageResults = listOf(
            MessageResult(message = message1, channel = channel),
            MessageResult(message = message2, channel = channel),
        )
        sut.state.test {
            skipItems(1) // Skip initial state

            searchScope.testScheduler.advanceUntilIdle()
            val initialActual = awaitItem()
            assertFalse(initialActual.isLoading)
            assertEquals(expectedFirstPageResults, initialActual.results)
            assertEquals("next", initialActual.nextPage)
            assertTrue(initialActual.canLoadMore)
            assertFalse(initialActual.isLoadingMore)

            sut.loadMore()
            assertTrue(awaitItem().isLoadingMore)

            sut.refresh()
            assertTrue(awaitItem().isLoading)

            searchScope.testScheduler.advanceUntilIdle()
            // Assert that the only results emitted after refresh is the first page results,
            // as the load more request should be skipped.
            val finalActual = awaitItem()
            assertFalse(finalActual.isLoading)
            assertEquals(expectedFirstPageResults, finalActual.results)
            assertEquals("next", finalActual.nextPage)
            assertTrue(finalActual.canLoadMore)
            assertFalse(finalActual.isLoadingMore)
        }
    }
}

private const val CHANNEL_ID = "123"
private const val CHANNEL_TYPE = "messaging"
private const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
private val User = User(id = "uid", name = "Username")

private class Fixture {

    private val sort: QuerySorter<Message> = mock()
    private val repositoryFacade: RepositoryFacade = mock()
    private val chatClient: ChatClient = mock {
        whenever(mock.repositoryFacade) doReturn repositoryFacade
    }

    fun givenCurrentUser(currentUser: User = User) = apply {
        whenever(chatClient.getCurrentUser()) doReturn currentUser
    }

    fun givenSearchMessagesResult(
        scope: CoroutineScope? = null,
        next: String?,
        result: SearchMessagesResult? = null,
        error: Error? = null,
    ) = apply {
        whenever(
            chatClient.searchMessages(
                channelFilter = any(),
                messageFilter = any(),
                offset = anyOrNull(),
                limit = anyOrNull(),
                next = eq(next),
                sort = eq(sort),
            ),
        ) doAnswer {
            if (scope != null) {
                // Mock a asynchronous call
                MockRetrofitCall(
                    scope = scope,
                    result = result?.let { Result.Success(it) } ?: error?.let { Result.Failure(it) }!!,
                    doWork = { delay(1) },
                )
            } else {
                result?.asCall() ?: error?.let { TestCall(Result.Failure(it)) }
            }
        }
    }

    suspend fun givenChannels(channels: List<Channel>) = apply {
        whenever(repositoryFacade.selectChannels(channelCIDs = any())) doReturn channels
    }

    fun get(scope: CoroutineScope): MentionListController =
        MentionListController(scope = scope, sort = sort, chatClient = chatClient)
}
