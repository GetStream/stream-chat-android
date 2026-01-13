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

package io.getstream.chat.android.ui.common.feature.messages.poll

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.QueryPollVotesResult
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PollOptionResultsViewControllerTest {

    @Test
    fun `when initialized, should set initial state with option and vote count`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 5),
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .get(backgroundScope)

        sut.state.test {
            val initialState = awaitItem()

            assertEquals(option, initialState.option)
            assertEquals(5, initialState.voteCount)
            assertTrue(initialState.isLoading)
            assertTrue(initialState.results.isEmpty())
            assertTrue(initialState.canLoadMore)
            assertFalse(initialState.isLoadingMore)
        }
    }

    @Test
    fun `when initialized with winner option, should mark as winner`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val option2 = Option(id = "opt2", text = "Option 2")
        val poll = randomPoll(
            options = listOf(option1, option2),
            voteCountsByOption = mapOf("opt1" to 10, "opt2" to 5),
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option1)
            .get(backgroundScope)

        sut.state.test {
            val initialState = awaitItem()

            assertTrue(initialState.isWinner) // option1 has highest vote count
        }
    }

    @Test
    fun `when initialized with non-winner option, should not mark as winner`() = runTest {
        val option1 = Option(id = "opt1", text = "Option 1")
        val option2 = Option(id = "opt2", text = "Option 2")
        val poll = randomPoll(
            options = listOf(option1, option2),
            voteCountsByOption = mapOf("opt1" to 5, "opt2" to 10),
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option1)
            .get(backgroundScope)

        sut.state.test {
            val initialState = awaitItem()

            assertFalse(initialState.isWinner) // option2 has highest vote count
        }
    }

    @Test
    fun `when initial load succeeds, should update state with votes`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 2),
        )
        val vote1 = randomPollVote(pollId = poll.id, optionId = option.id)
        val vote2 = randomPollVote(pollId = poll.id, optionId = option.id)
        val nextPage = randomString()
        val queryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote1, vote2),
            next = nextPage,
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .givenQueryPollVotesResult(result = queryPollVotesResult)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val viewState = awaitItem()

            assertFalse(viewState.isLoading)
            assertEquals(listOf(vote1, vote2), viewState.results)
            assertTrue(viewState.canLoadMore)
            assertFalse(viewState.isLoadingMore)
        }
    }

    @Test
    fun `when initial load fails, should emit error event and set isLoading to false`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 0),
        )
        val error = Error.GenericError("error")
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .givenQueryPollVotesResult(error = error)
            .get(backgroundScope)

        sut.events.test {
            sut.state.test {
                skipItems(1) // Skip initial state
                val viewState = awaitItem()

                assertFalse(viewState.isLoading)
                assertTrue(viewState.results.isEmpty())
            }

            val event = awaitItem()
            assertInstanceOf<PollOptionResultsViewEvent.LoadError>(event)
            assertEquals(error, event.error)
        }
    }

    @Test
    fun `when load more succeeds, should append votes`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 2),
        )
        val vote1 = randomPollVote(pollId = poll.id, optionId = option.id)
        val nextPage = randomString()
        val firstQueryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote1),
            next = nextPage,
        )
        val vote2 = randomPollVote(pollId = poll.id, optionId = option.id)
        val secondQueryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote2),
            next = null,
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .givenQueryPollVotesResult(next = null, result = firstQueryPollVotesResult)
            .givenQueryPollVotesResult(next = nextPage, result = secondQueryPollVotesResult)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val firstPageViewState = awaitItem()
            assertFalse(firstPageViewState.isLoading)
            assertEquals(listOf(vote1), firstPageViewState.results)
            assertTrue(firstPageViewState.canLoadMore)
            assertFalse(firstPageViewState.isLoadingMore)

            sut.onViewAction(PollOptionResultsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem()
            assertFalse(loadingMoreViewState.isLoading)
            assertTrue(loadingMoreViewState.canLoadMore)
            assertTrue(loadingMoreViewState.isLoadingMore)

            val finalViewState = awaitItem()
            assertFalse(finalViewState.isLoading)
            assertEquals(listOf(vote1, vote2), finalViewState.results)
            assertFalse(finalViewState.canLoadMore)
            assertFalse(finalViewState.isLoadingMore)
        }
    }

    @Test
    fun `when load more fails, should emit error event and retain state`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 1),
        )
        val vote1 = randomPollVote(pollId = poll.id, optionId = option.id)
        val nextPage = randomString()
        val firstQueryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote1),
            next = nextPage,
        )
        val searchingError = Error.GenericError("error")
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .givenQueryPollVotesResult(result = firstQueryPollVotesResult)
            .givenQueryPollVotesResult(next = nextPage, error = searchingError)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val firstPageViewState = awaitItem()
            assertFalse(firstPageViewState.isLoading)
            assertFalse(firstPageViewState.isLoadingMore)

            sut.onViewAction(PollOptionResultsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem()
            assertFalse(loadingMoreViewState.isLoading)
            assertTrue(loadingMoreViewState.isLoadingMore)

            sut.events.test {
                val event = awaitItem()
                assertInstanceOf<PollOptionResultsViewEvent.LoadError>(event)
                assertEquals(searchingError, event.error)
            }

            val finalViewState = awaitItem()
            assertFalse(finalViewState.isLoading)
            assertEquals(listOf(vote1), finalViewState.results)
            assertTrue(finalViewState.canLoadMore)
            assertFalse(finalViewState.isLoadingMore)
        }
    }

    @Test
    fun `when no more votes to load, should not load more`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 1),
        )
        val vote1 = randomPollVote(pollId = poll.id, optionId = option.id)
        val queryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote1),
            next = null,
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .givenQueryPollVotesResult(result = queryPollVotesResult)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val viewState = awaitItem()
            assertFalse(viewState.isLoading)
            assertFalse(viewState.canLoadMore)
            assertFalse(viewState.isLoadingMore)

            sut.onViewAction(PollOptionResultsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }

    @Test
    fun `when already loading more, should not emit duplicate events`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 1),
        )
        val nextPage = randomString()
        val queryPollVotesResult = QueryPollVotesResult(
            votes = listOf(randomPollVote(pollId = poll.id, optionId = option.id)),
            next = nextPage,
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .givenQueryPollVotesResult(result = queryPollVotesResult)
            .givenQueryPollVotesResult(next = nextPage, result = queryPollVotesResult)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val viewState = awaitItem()
            assertFalse(viewState.isLoading)
            assertFalse(viewState.isLoadingMore)

            sut.onViewAction(PollOptionResultsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem()
            assertFalse(loadingMoreViewState.isLoading)
            assertTrue(loadingMoreViewState.isLoadingMore)

            sut.onViewAction(PollOptionResultsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }

    @Test
    fun `when in initial loading state, should not allow load more`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 0),
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .givenQueryPollVotesResult(result = QueryPollVotesResult(votes = emptyList(), next = null))
            .get(backgroundScope)

        sut.state.test {
            val viewState = awaitItem() // initial loading state
            assertTrue(viewState.isLoading)
            sut.onViewAction(PollOptionResultsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }

    @Test
    fun `when querying votes, should use correct filter for option`() = runTest {
        val option = Option(id = "opt1", text = "Option 1")
        val poll = randomPoll(
            id = "poll1",
            options = listOf(option),
            voteCountsByOption = mapOf("opt1" to 1),
        )
        val vote1 = randomPollVote(pollId = poll.id, optionId = option.id)
        val queryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote1),
            next = null,
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenOption(option)
            .givenQueryPollVotesResult(result = queryPollVotesResult)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()
        }

        // Verify that queryPollVotes was called with correct filter
        // This is verified implicitly through the test setup in Fixture
    }
}

private class Fixture {

    private var poll: Poll = randomPoll()
    private var option: Option = poll.options.first()
    private val chatClient: ChatClient = mock()

    fun givenPoll(poll: Poll) = apply {
        this.poll = poll
    }

    fun givenOption(option: Option) = apply {
        this.option = option
    }

    fun givenQueryPollVotesResult(
        next: String? = null,
        result: QueryPollVotesResult? = null,
        error: Error? = null,
    ) = apply {
        whenever(
            chatClient.queryPollVotes(
                pollId = poll.id,
                filter = Filters.eq("option_id", option.id),
                limit = 25,
                next = next,
                sort = QuerySortByField.descByName("created_at"),
            ),
        ) doAnswer { result?.asCall() ?: error?.asCall() }
    }

    fun get(scope: CoroutineScope) = PollOptionResultsViewController(
        poll = poll,
        option = option,
        chatClient = chatClient,
        scope = scope,
    )
}
