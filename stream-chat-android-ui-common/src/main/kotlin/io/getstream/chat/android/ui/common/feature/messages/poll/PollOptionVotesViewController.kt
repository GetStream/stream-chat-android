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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.internal.getWinner
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.QueryPollVotesResult
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.ui.common.feature.messages.poll.PollOptionVotesViewAction.LoadMoreRequested
import io.getstream.chat.android.ui.common.state.messages.poll.PollOptionVotesViewState
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/**
 * Controller responsible for managing the state and events related to poll option votes.
 *
 * This controller handles fetching votes for a specific poll option from the API with pagination
 * support. It automatically loads the first page on initialization and supports loading additional
 * pages when [PollOptionVotesViewAction.LoadMoreRequested] is triggered. The controller emits
 * state updates and error events for the UI to react to.
 *
 * @param poll The poll containing the option for which votes are displayed.
 * @param option The specific poll option for which vote results are fetched.
 * @param chatClient The [ChatClient] instance used for querying poll votes from the API.
 * @param scope The [CoroutineScope] used for launching coroutines.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@InternalStreamChatApi
public class PollOptionVotesViewController(
    private val poll: Poll,
    private val option: Option,
    private val chatClient: ChatClient = ChatClient.instance(),
    scope: CoroutineScope,
) {

    private val logger by taggedLogger("Chat:PollOptionVotesViewController")

    /**
     * This flow is used to trigger the loading of votes when needed.
     */
    private val loadRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * The next page token for pagination.
     */
    private var nextPage: String? = null

    private val _state = MutableStateFlow(
        PollOptionVotesViewState(
            option = option,
            voteCount = poll.voteCountsByOption[option.id] ?: 0,
            isWinner = poll.getWinner() == option,
        ),
    )

    /**
     * The current state of the poll option votes view.
     */
    public val state: StateFlow<PollOptionVotesViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PollOptionVotesViewEvent>(extraBufferCapacity = 1)

    /**
     * One shot events triggered by the controller.
     */
    public val events: SharedFlow<PollOptionVotesViewEvent> = _events.asSharedFlow()

    init {
        loadRequests.onStart { emit(Unit) } // Triggers the initial load
            .flatMapLatest { flowOf(fetchPollVotes()) }
            .onEach { result ->
                result
                    .onSuccess(::onSuccessResult)
                    .onError(::onFailureResult)
            }
            .launchIn(scope)
    }

    /**
     * Handles an [PollOptionVotesViewAction] coming from the View layer.
     */
    public fun onViewAction(action: PollOptionVotesViewAction) {
        when (action) {
            PollOptionVotesViewAction.LoadMoreRequested -> loadMore()
        }
    }

    private suspend fun fetchPollVotes(): Result<QueryPollVotesResult> {
        logger.d {
            "[fetchPollVotes] Fetching votes for poll: ${poll.id}, option: ${option.id}, " +
                "limit: $QUERY_LIMIT, next: $nextPage"
        }
        return chatClient.queryPollVotes(
            pollId = poll.id,
            filter = Filters.eq("option_id", option.id),
            limit = QUERY_LIMIT,
            next = nextPage,
            sort = QuerySortByField.descByName("created_at"),
        ).await()
    }

    private fun onSuccessResult(result: QueryPollVotesResult) {
        nextPage = result.next
        val votes = result.votes
        logger.d { "[onSuccessResult] Fetched ${votes.size} votes, next: $nextPage" }
        _state.update { currentState ->
            val currentItems = if (!currentState.isLoading) {
                currentState.results
            } else {
                emptyList()
            }
            currentState.copy(
                isLoading = false,
                results = currentItems + votes,
                canLoadMore = nextPage != null,
                isLoadingMore = false,
            )
        }
    }

    private fun onFailureResult(error: Error) {
        logger.e { "[onFailureResult] error: ${error.message}" }
        _events.tryEmit(PollOptionVotesViewEvent.LoadError(error))
        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                isLoadingMore = false,
            )
        }
    }

    private fun loadMore() {
        val currentState = state.value
        if (currentState.isLoading) {
            logger.d { "[loadMore] still loading initial votes, cannot load more" }
            return
        }

        if (!currentState.canLoadMore) {
            logger.d { "[loadMore] no more votes to load" }
            return
        }

        if (currentState.isLoadingMore) {
            logger.d { "[loadMore] already loading more votes" }
            return
        }

        logger.d { "[loadMore] loading more votes" }
        _state.value = currentState.copy(isLoadingMore = true)
        loadRequests.tryEmit(Unit)
    }
}

private const val QUERY_LIMIT = 25
