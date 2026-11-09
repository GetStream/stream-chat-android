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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.QueryPollAnswersResult
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.ui.common.feature.messages.poll.PollAnswersViewAction.LoadMoreRequested
import io.getstream.chat.android.ui.common.state.messages.poll.PollAnswersViewState
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.util.Collections

/**
 * Controller for the paginated list of a poll's answers (comments), newest first.
 *
 * Answers cast, edited or removed while the list is open are applied to it.
 *
 * @param pollId The id of the poll whose answers are listed.
 * @param chatClient The [ChatClient] used to query the answers and observe events.
 * @param scope The [CoroutineScope] the controller lives in. Cancelling it stops observing events.
 * @param cachedAnswers Provides the answers already known locally, shown if the first page fails to load.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@InternalStreamChatApi
public class PollAnswersViewController(
    private val pollId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    scope: CoroutineScope,
    private val cachedAnswers: () -> List<Answer> = { emptyList() },
) {

    private val logger by taggedLogger("Chat:PollAnswersViewController")

    private val loadRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var nextPage: String? = null

    // Ids removed live, so a page fetched before the removal doesn't bring them back.
    // Written from the event thread and read when a page arrives.
    private val removedAnswerIds: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())

    private val _state = MutableStateFlow(PollAnswersViewState())

    /**
     * The current state of the poll answers view.
     */
    public val state: StateFlow<PollAnswersViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PollAnswersViewEvent>(extraBufferCapacity = 1)

    /**
     * One shot events triggered by the controller.
     */
    public val events: SharedFlow<PollAnswersViewEvent> = _events.asSharedFlow()

    init {
        loadRequests.onStart { emit(Unit) } // Triggers the initial load
            .flatMapLatest { flowOf(fetchPollAnswers()) }
            .onEach { result ->
                result
                    .onSuccess(::onSuccessResult)
                    .onError(::onFailureResult)
            }
            .launchIn(scope)

        val subscription = chatClient.subscribe { event -> onChatEvent(event) }
        scope.coroutineContext[Job]?.invokeOnCompletion { subscription.dispose() }
    }

    /**
     * Handles a [PollAnswersViewAction] coming from the View layer.
     */
    public fun onViewAction(action: PollAnswersViewAction) {
        when (action) {
            LoadMoreRequested -> loadMore()
        }
    }

    private suspend fun fetchPollAnswers(): Result<QueryPollAnswersResult> {
        logger.d { "[fetchPollAnswers] poll: $pollId, limit: $QUERY_LIMIT, next: $nextPage" }
        return chatClient.queryPollAnswers(
            pollId = pollId,
            limit = QUERY_LIMIT,
            next = nextPage,
            sort = QuerySortByField.descByName("created_at"),
        ).await()
    }

    private fun onSuccessResult(result: QueryPollAnswersResult) {
        nextPage = result.next
        logger.d { "[onSuccessResult] Fetched ${result.answers.size} answers, next: $nextPage" }
        _state.update { currentState ->
            // Answers received live, even during the first load, are kept and win over the page's copy
            val page = result.answers.withoutStale(currentState.results)
            currentState.copy(
                isLoading = false,
                results = (currentState.results + page).distinctBy { it.id },
                canLoadMore = nextPage != null,
                isLoadingMore = false,
            )
        }
    }

    private fun onFailureResult(error: Error) {
        logger.e { "[onFailureResult] error: ${error.message}" }
        _events.tryEmit(PollAnswersViewEvent.LoadError(error))
        _state.update { currentState ->
            if (!currentState.isLoading) return@update currentState.copy(isLoadingMore = false)
            // A failed first load falls back to the answers known locally; with no cursor, paging stops there
            val fallback = cachedAnswers().withoutStale(currentState.results)
            currentState.copy(
                isLoading = false,
                results = (currentState.results + fallback).distinctBy { it.id }.sortedByDescending { it.createdAt },
                canLoadMore = false,
                isLoadingMore = false,
            )
        }
    }

    /**
     * Drops answers removed live, and answers whose author already has one in [current]: a user has a single answer,
     * so the other copy is stale.
     */
    private fun List<Answer>.withoutStale(current: List<Answer>): List<Answer> {
        val authorIds = current.mapNotNull { it.user?.id }.toSet()
        return filterNot { it.id in removedAnswerIds || it.user?.id in authorIds }
    }

    private fun onChatEvent(event: ChatEvent) {
        when (event) {
            is AnswerCastedEvent -> if (event.poll.id == pollId) upsertAnswer(event.newAnswer)
            is VoteRemovedEvent -> if (event.poll.id == pollId) removeAnswer(event.removedVote.id)
            else -> Unit
        }
    }

    private fun upsertAnswer(answer: Answer) {
        _state.update { currentState ->
            val results = currentState.results
            val index = results.indexOfFirst { it.id == answer.id }
            val updated = if (index >= 0) {
                results.toMutableList().apply { set(index, answer) }
            } else {
                // A user has a single answer, so an edit arriving with a new id replaces theirs
                val authorId = answer.user?.id
                listOf(answer) + results.filterNot { authorId != null && it.user?.id == authorId }
            }
            currentState.copy(results = updated)
        }
    }

    private fun removeAnswer(answerId: String) {
        removedAnswerIds += answerId
        _state.update { currentState ->
            currentState.copy(results = currentState.results.filterNot { it.id == answerId })
        }
    }

    private fun loadMore() {
        val currentState = state.value
        if (currentState.isLoading) {
            logger.d { "[loadMore] still loading initial answers, cannot load more" }
            return
        }
        if (!currentState.canLoadMore) {
            logger.d { "[loadMore] no more answers to load" }
            return
        }
        if (currentState.isLoadingMore) {
            logger.d { "[loadMore] already loading more answers" }
            return
        }
        logger.d { "[loadMore] loading more answers" }
        _state.update { it.copy(isLoadingMore = true) }
        loadRequests.tryEmit(Unit)
    }
}

private const val QUERY_LIMIT = 25
