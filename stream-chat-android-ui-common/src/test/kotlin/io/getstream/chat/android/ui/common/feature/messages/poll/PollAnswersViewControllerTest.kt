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

package io.getstream.chat.android.ui.common.feature.messages.poll

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.QueryPollAnswersResult
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollAnswer
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewAction.LoadMoreRequested
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class PollAnswersViewControllerTest {

    @Test
    fun `when initialized, should be loading with no results`() = runTest {
        val sut = AnswersFixture().get(backgroundScope)

        sut.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)
            assertTrue(initialState.results.isEmpty())
            assertTrue(initialState.canLoadMore)
            assertFalse(initialState.isLoadingMore)
        }
    }

    @Test
    fun `when initial load succeeds, should show the first page`() = runTest {
        val fixture = AnswersFixture()
        val answers = listOf(fixture.answer(), fixture.answer())
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = answers, next = randomString()))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val viewState = awaitItem()
            assertFalse(viewState.isLoading)
            assertEquals(answers, viewState.results)
            assertTrue(viewState.canLoadMore)
        }
    }

    @Test
    fun `when initial load fails, should emit error event and stop loading`() = runTest {
        val error = Error.GenericError("error")
        val sut = AnswersFixture()
            .givenQueryResult(error = error)
            .get(backgroundScope)

        sut.events.test {
            sut.state.test {
                skipItems(1) // Skip initial state
                val viewState = awaitItem()
                assertFalse(viewState.isLoading)
                assertTrue(viewState.results.isEmpty())
            }
            val event = awaitItem()
            assertInstanceOf<PollAnswersViewEvent.LoadError>(event)
            assertEquals(error, event.error)
        }
    }

    @Test
    fun `when initial load fails, should show the cached answers newest first`() = runTest {
        val fixture = AnswersFixture()
        val older = fixture.answer().copy(createdAt = Date(1_000))
        val newer = fixture.answer().copy(createdAt = Date(2_000))
        val sut = fixture
            .givenQueryResult(error = Error.GenericError("error"))
            .get(backgroundScope, cachedAnswers = listOf(older, newer))

        sut.state.test {
            skipItems(1) // Skip initial state
            val viewState = awaitItem()
            assertEquals(listOf(newer, older), viewState.results)
            assertFalse(viewState.canLoadMore)
        }
    }

    @Test
    fun `when initial load fails after live changes, should keep them over the cached answers`() = runTest {
        val fixture = AnswersFixture()
        val author = randomUser()
        val stale = fixture.answer(user = author).copy(createdAt = Date(1_000))
        val removed = fixture.answer().copy(createdAt = Date(2_000))
        val kept = fixture.answer().copy(createdAt = Date(3_000))
        val pending = fixture.givenPendingQuery()
        val sut = fixture.get(backgroundScope, cachedAnswers = listOf(stale, removed, kept))

        sut.state.test {
            skipItems(1) // Skip initial state
            val edited = fixture.answer(user = author).copy(createdAt = Date(4_000))
            fixture.emit(fixture.answerCastedEvent(edited))
            awaitItem()
            fixture.emit(fixture.voteRemovedEvent(removed.id))

            pending.complete(Result.Failure(Error.GenericError("error")))
            assertEquals(listOf(edited, kept), awaitItem().results)
        }
    }

    @Test
    fun `when initial load fails, should not load more`() = runTest {
        val fixture = AnswersFixture()
        val sut = fixture
            .givenQueryResult(error = Error.GenericError("error"))
            .get(backgroundScope, cachedAnswers = listOf(fixture.answer()))

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            sut.onViewAction(LoadMoreRequested)
            expectNoEvents()
        }
    }

    @Test
    fun `when load more fails, should not add the cached answers`() = runTest {
        val fixture = AnswersFixture()
        val loaded = fixture.answer()
        val cached = fixture.answer()
        val nextPage = randomString()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(loaded), next = nextPage))
            .givenQueryResult(next = nextPage, error = Error.GenericError("error"))
            .get(backgroundScope, cachedAnswers = listOf(cached))

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            sut.onViewAction(LoadMoreRequested)
            awaitItem() // Loading more
            assertEquals(listOf(loaded), awaitItem().results)
        }
    }

    @Test
    fun `when load more succeeds, should append the next page`() = runTest {
        val fixture = AnswersFixture()
        val first = fixture.answer()
        val second = fixture.answer()
        val nextPage = randomString()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(first), next = nextPage))
            .givenQueryResult(next = nextPage, result = QueryPollAnswersResult(answers = listOf(second), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            assertEquals(listOf(first), awaitItem().results)

            sut.onViewAction(LoadMoreRequested)
            assertTrue(awaitItem().isLoadingMore)

            val finalState = awaitItem()
            assertEquals(listOf(first, second), finalState.results)
            assertFalse(finalState.canLoadMore)
            assertFalse(finalState.isLoadingMore)
        }
    }

    @Test
    fun `when no more answers to load, should not load more`() = runTest {
        val fixture = AnswersFixture()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(fixture.answer()), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            assertFalse(awaitItem().canLoadMore)

            sut.onViewAction(LoadMoreRequested)
            expectNoEvents()
        }
    }

    @Test
    fun `when an answer is cast live, should add it on top`() = runTest {
        val fixture = AnswersFixture()
        val existing = fixture.answer()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(existing), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            val live = fixture.answer()
            fixture.emit(fixture.answerCastedEvent(live))
            assertEquals(listOf(live, existing), awaitItem().results)
        }
    }

    @Test
    fun `when an answer is edited live with the same id, should replace it in place`() = runTest {
        val fixture = AnswersFixture()
        val first = fixture.answer()
        val second = fixture.answer()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(first, second), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            val edited = second.copy(text = "edited")
            fixture.emit(fixture.answerCastedEvent(edited))
            assertEquals(listOf(first, edited), awaitItem().results)
        }
    }

    @Test
    fun `when a user's answer arrives live with a new id, should replace their previous one`() = runTest {
        val fixture = AnswersFixture()
        val author = randomUser()
        val previous = fixture.answer(user = author)
        val other = fixture.answer()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(other, previous), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            val replacement = fixture.answer(user = author)
            fixture.emit(fixture.answerCastedEvent(replacement))
            assertEquals(listOf(replacement, other), awaitItem().results)
        }
    }

    @Test
    fun `when an answer is removed live, should drop it`() = runTest {
        val fixture = AnswersFixture()
        val kept = fixture.answer()
        val removed = fixture.answer()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(kept, removed), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            fixture.emit(fixture.voteRemovedEvent(removed.id))
            assertEquals(listOf(kept), awaitItem().results)
        }
    }

    @Test
    fun `when events concern another poll, should ignore them`() = runTest {
        val fixture = AnswersFixture()
        val existing = fixture.answer()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(existing), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            fixture.emit(fixture.answerCastedEvent(fixture.answer(), poll = randomPoll()))
            fixture.emit(fixture.voteRemovedEvent(existing.id, poll = randomPoll()))
            expectNoEvents()
        }
    }

    @Test
    fun `when a live answer comes back in a later page, should not duplicate it`() = runTest {
        val fixture = AnswersFixture()
        val first = fixture.answer()
        val live = fixture.answer()
        val nextPage = randomString()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(first), next = nextPage))
            .givenQueryResult(next = nextPage, result = QueryPollAnswersResult(answers = listOf(live), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            fixture.emit(fixture.answerCastedEvent(live))
            assertEquals(listOf(live, first), awaitItem().results)

            sut.onViewAction(LoadMoreRequested)
            awaitItem() // Loading more
            assertEquals(listOf(live, first), awaitItem().results)
        }
    }

    @Test
    fun `when a later page has an older answer from a live author, should keep only the live one`() = runTest {
        val fixture = AnswersFixture()
        val author = randomUser()
        val first = fixture.answer()
        val stale = fixture.answer(user = author)
        val nextPage = randomString()
        val sut = fixture
            .givenQueryResult(result = QueryPollAnswersResult(answers = listOf(first), next = nextPage))
            .givenQueryResult(next = nextPage, result = QueryPollAnswersResult(answers = listOf(stale), next = null))
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            val edited = fixture.answer(user = author)
            fixture.emit(fixture.answerCastedEvent(edited))
            assertEquals(listOf(edited, first), awaitItem().results)

            sut.onViewAction(LoadMoreRequested)
            awaitItem() // Loading more
            assertEquals(listOf(edited, first), awaitItem().results)
        }
    }

    @Test
    fun `when the scope is cancelled, should stop observing events`() = runTest {
        val fixture = AnswersFixture()
        val job = Job()
        fixture.get(CoroutineScope(coroutineContext + job))

        job.cancel()
        advanceUntilIdle()

        verify(fixture.disposable).dispose()
    }

    @Test
    fun `when built with a scope without a Job, should not crash`() = runTest {
        // The CoroutineScope() factory would add a Job
        val scope = object : CoroutineScope {
            override val coroutineContext = UnconfinedTestDispatcher(testScheduler)
        }
        AnswersFixture().get(scope)
    }

    @Test
    fun `when an answer is cast during the initial load, should keep it`() = runTest {
        val fixture = AnswersFixture()
        val pending = fixture.givenPendingQuery()
        val sut = fixture.get(backgroundScope)
        val first = fixture.answer()
        val live = fixture.answer()

        sut.state.test {
            assertTrue(awaitItem().isLoading)

            fixture.emit(fixture.answerCastedEvent(live))
            assertEquals(listOf(live), awaitItem().results)

            pending.complete(QueryPollAnswersResult(answers = listOf(first), next = null).asSuccess())
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(listOf(live, first), loaded.results)
        }
    }

    @Test
    fun `when an answer is removed during the initial load, should not show it from the page`() = runTest {
        val fixture = AnswersFixture()
        val pending = fixture.givenPendingQuery()
        val sut = fixture.get(backgroundScope)
        val removed = fixture.answer()
        val kept = fixture.answer()

        sut.state.test {
            assertTrue(awaitItem().isLoading)

            fixture.emit(fixture.voteRemovedEvent(removed.id))
            pending.complete(QueryPollAnswersResult(answers = listOf(removed, kept), next = null).asSuccess())
            assertEquals(listOf(kept), awaitItem().results)
        }
    }

    @Test
    fun `when an answer is removed while loading more, should not show it from the next page`() = runTest {
        val fixture = AnswersFixture()
        val first = fixture.answer()
        val removed = fixture.answer()
        val kept = fixture.answer()
        val nextPage = randomString()
        fixture.givenQueryResult(result = QueryPollAnswersResult(answers = listOf(first), next = nextPage))
        val pending = fixture.givenPendingQuery(next = nextPage)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            awaitItem()

            sut.onViewAction(LoadMoreRequested)
            assertTrue(awaitItem().isLoadingMore)

            fixture.emit(fixture.voteRemovedEvent(removed.id))
            pending.complete(QueryPollAnswersResult(answers = listOf(removed, kept), next = null).asSuccess())
            assertEquals(listOf(first, kept), awaitItem().results)
        }
    }
}

private fun QueryPollAnswersResult.asSuccess(): Result<QueryPollAnswersResult> = Result.Success(this)

/**
 * A [Call] whose result is the one the test completes [result] with.
 */
private class PendingCall<T : Any>(val result: CompletableDeferred<Result<T>>) : Call<T> {
    override fun cancel() = Unit
    override fun enqueue(callback: Call.Callback<T>) = error("Not used")
    override fun execute(): Result<T> = error("Not used")
    override suspend fun await(): Result<T> = result.await()
}

private class AnswersFixture {

    val poll: Poll = randomPoll()
    val disposable: Disposable = mock()
    private val chatClient: ChatClient = mock()
    private var listener: ((ChatEvent) -> Unit)? = null

    init {
        whenever(chatClient.subscribe(any())) doAnswer { invocation ->
            val chatEventListener = invocation.getArgument<ChatEventListener<ChatEvent>>(0)
            listener = { chatEventListener.onEvent(it) }
            disposable
        }
        givenQueryResult(result = QueryPollAnswersResult(answers = emptyList(), next = null))
    }

    fun givenPendingQuery(next: String? = null): CompletableDeferred<Result<QueryPollAnswersResult>> {
        val result = CompletableDeferred<Result<QueryPollAnswersResult>>()
        whenever(
            chatClient.queryPollAnswers(
                pollId = poll.id,
                filter = null,
                limit = 25,
                next = next,
                sort = QuerySortByField.descByName("created_at"),
            ),
        ) doAnswer { PendingCall(result) }
        return result
    }

    fun givenQueryResult(
        next: String? = null,
        result: QueryPollAnswersResult? = null,
        error: Error? = null,
    ) = apply {
        whenever(
            chatClient.queryPollAnswers(
                pollId = poll.id,
                filter = null,
                limit = 25,
                next = next,
                sort = QuerySortByField.descByName("created_at"),
            ),
        ) doAnswer { result?.asCall() ?: error?.asCall() }
    }

    fun answer(user: io.getstream.chat.android.models.User = randomUser()): Answer =
        randomPollAnswer(pollId = poll.id, user = user)

    fun emit(event: ChatEvent) {
        listener?.invoke(event)
    }

    fun answerCastedEvent(answer: Answer, poll: Poll = this.poll) = AnswerCastedEvent(
        type = EventType.POLL_VOTE_CASTED,
        createdAt = Date(),
        rawCreatedAt = null,
        cid = "messaging:123",
        channelType = "messaging",
        channelId = "123",
        messageId = randomString(),
        poll = poll,
        newAnswer = answer,
    )

    fun voteRemovedEvent(voteId: String, poll: Poll = this.poll) = VoteRemovedEvent(
        type = EventType.POLL_VOTE_REMOVED,
        createdAt = Date(),
        rawCreatedAt = null,
        cid = "messaging:123",
        channelType = "messaging",
        channelId = "123",
        messageId = randomString(),
        poll = poll,
        removedVote = randomPollVote(id = voteId, pollId = poll.id),
    )

    fun get(scope: CoroutineScope, cachedAnswers: List<Answer> = emptyList()) = PollAnswersViewController(
        pollId = poll.id,
        chatClient = chatClient,
        scope = scope,
        cachedAnswers = { cachedAnswers },
    )
}
