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
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.QueryPollVotesResult
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.messages.poll.PollResultsViewState
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

internal class PollResultsViewControllerTest {

    @Test
    fun `when initialized with non-anonymous poll, should set state to Loading`() = runTest {
        val poll = randomPoll(votingVisibility = VotingVisibility.PUBLIC)
        val sut = Fixture()
            .givenPoll(poll)
            .get(backgroundScope)

        sut.state.value.let { state ->
            assertInstanceOf<PollResultsViewState.Loading>(state)
        }
    }

    @Test
    fun `when initialized with anonymous poll, should set state to Content`() = runTest {
        val poll = randomPoll(votingVisibility = VotingVisibility.ANONYMOUS)
        val sut = Fixture()
            .givenPoll(poll)
            .get(backgroundScope)

        sut.state.value.let { state ->
            assertInstanceOf<PollResultsViewState.Content>(state)
            assertEquals(poll, state.poll)
            assertFalse(state.canLoadMore)
            assertFalse(state.isLoadingMore)
        }
    }

    @Test
    fun `when initial load succeeds, should update state with content`() = runTest {
        val poll = randomPoll(
            votingVisibility = VotingVisibility.PUBLIC,
            votes = listOf(randomPollVote(), randomPollVote()),
        )
        val vote1 = randomPollVote(pollId = poll.id)
        val vote2 = randomPollVote(pollId = poll.id)
        val nextPage = randomString()
        val queryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote1, vote2),
            next = nextPage,
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenQueryPollVotesResult(result = queryPollVotesResult)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val viewState = awaitItem()

            assertInstanceOf<PollResultsViewState.Content>(viewState)
            val expectedVotes = listOf(vote1, vote2)
            assertEquals(expectedVotes, viewState.poll.votes)
            assertTrue(viewState.canLoadMore)
            assertFalse(viewState.isLoadingMore)
        }
    }

    @Test
    fun `when initial load fails, should update state with error`() = runTest {
        val poll = randomPoll(votingVisibility = VotingVisibility.PUBLIC)
        val error = Error.GenericError("error")
        val sut = Fixture()
            .givenPoll(poll)
            .givenQueryPollVotesResult(error = error)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val viewState = awaitItem()

            assertInstanceOf<PollResultsViewState.Error>(viewState)
            assertEquals("error", viewState.message)
            assertEquals(poll, viewState.poll)
        }
    }

    @Test
    fun `when load more succeeds, should append votes to state`() = runTest {
        val poll = randomPoll(votingVisibility = VotingVisibility.PUBLIC)
        val vote1 = randomPollVote(pollId = poll.id)
        val nextPage = randomString()
        val firstQueryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote1),
            next = nextPage,
        )
        val vote2 = randomPollVote(pollId = poll.id)
        val secondQueryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote2),
            next = null,
        )
        val sut = Fixture()
            .givenPoll(poll)
            .givenQueryPollVotesResult(next = null, result = firstQueryPollVotesResult)
            .givenQueryPollVotesResult(next = nextPage, result = secondQueryPollVotesResult)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val expectedFirstPageVotes = listOf(vote1)
            val firstPageViewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(firstPageViewState)
            assertEquals(expectedFirstPageVotes, firstPageViewState.poll.votes)
            assertTrue(firstPageViewState.canLoadMore)

            sut.onViewAction(PollResultsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(loadingMoreViewState)
            assertEquals(expectedFirstPageVotes, loadingMoreViewState.poll.votes)
            assertTrue(loadingMoreViewState.canLoadMore)
            assertTrue(loadingMoreViewState.isLoadingMore)

            val expectedAccumulatedVotes = expectedFirstPageVotes + listOf(vote2)
            val finalViewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(finalViewState)
            assertEquals(expectedAccumulatedVotes, finalViewState.poll.votes)
            assertFalse(finalViewState.canLoadMore)
            assertFalse(finalViewState.isLoadingMore)
        }
    }

    @Test
    fun `when load more fails, should emit error event and retain state`() = runTest {
        val poll = randomPoll(votingVisibility = VotingVisibility.PUBLIC)
        val vote1 = randomPollVote(pollId = poll.id)
        val nextPage = randomString()
        val firstQueryPollVotesResult = QueryPollVotesResult(
            votes = listOf(vote1),
            next = nextPage,
        )
        val searchingError = Error.GenericError("error")
        val sut = Fixture()
            .givenPoll(poll)
            .givenQueryPollVotesResult(result = firstQueryPollVotesResult)
            .givenQueryPollVotesResult(next = nextPage, error = searchingError)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val firstPageViewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(firstPageViewState)
            assertFalse(firstPageViewState.isLoadingMore)

            sut.onViewAction(PollResultsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(loadingMoreViewState)
            assertTrue(loadingMoreViewState.isLoadingMore)

            sut.events.test {
                val event = awaitItem()
                assertInstanceOf<PollResultsViewEvent.LoadError>(event)
                assertEquals(searchingError, event.error)
            }

            val expectedFinalVotes = listOf(vote1)
            val finalViewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(finalViewState)
            assertEquals(expectedFinalVotes, finalViewState.poll.votes)
            assertTrue(finalViewState.canLoadMore)
            assertFalse(finalViewState.isLoadingMore)
        }
    }

    @Test
    fun `when no more votes to load, should not emit events`() = runTest {
        val poll = randomPoll(votingVisibility = VotingVisibility.PUBLIC)
        val sut = Fixture()
            .givenPoll(poll)
            .givenQueryPollVotesResult(result = QueryPollVotesResult(votes = emptyList(), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val viewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(viewState)
            assertFalse(viewState.canLoadMore)
            assertFalse(viewState.isLoadingMore)

            sut.onViewAction(PollResultsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }

    @Test
    fun `when already loading more, should not emit duplicate events`() = runTest {
        val poll = randomPoll(votingVisibility = VotingVisibility.PUBLIC)
        val sut = Fixture()
            .givenPoll(poll)
            .givenQueryPollVotesResult(result = QueryPollVotesResult(votes = emptyList(), next = randomString()))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val viewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(viewState)
            assertFalse(viewState.isLoadingMore)

            sut.onViewAction(PollResultsViewAction.LoadMoreRequested)

            val loadingMoreViewState = awaitItem()
            assertInstanceOf<PollResultsViewState.Content>(loadingMoreViewState)
            assertTrue(loadingMoreViewState.isLoadingMore)

            sut.onViewAction(PollResultsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }

    @Test
    fun `when in initial loading state, should not allow load more`() = runTest {
        val poll = randomPoll(votingVisibility = VotingVisibility.PUBLIC)
        val sut = Fixture()
            .givenPoll(poll)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.onViewAction(PollResultsViewAction.LoadMoreRequested)

            expectNoEvents()
        }
    }
}

private class Fixture {

    private var poll: Poll = randomPoll()
    private val chatClient: ChatClient = mock()

    fun givenPoll(poll: Poll) = apply {
        this.poll = poll
    }

    fun givenQueryPollVotesResult(
        next: String? = null,
        result: QueryPollVotesResult? = null,
        error: Error? = null,
    ) = apply {
        whenever(
            chatClient.queryPollVotes(
                pollId = poll.id,
                filter = null,
                limit = 10,
                next = next,
                sort = QuerySortByField.descByName("created_at"),
            ),
        ) doAnswer { result?.asCall() ?: error?.asCall() }
    }

    fun get(scope: CoroutineScope) = PollResultsViewController(
        poll = poll,
        chatClient = chatClient,
        scope = scope,
    )
}
