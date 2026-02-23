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

package io.getstream.chat.android.ui.viewmodel.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.state.utils.Event
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for searching for messages that match a particular search query.
 */
public class SearchViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val _state: MutableLiveData<State> = MutableLiveData(State())

    /**
     * The current state of the search screen.
     */
    public val state: LiveData<State> = _state

    private val _errorEvents: MutableLiveData<Event<Unit>> = MutableLiveData()

    /**
     * One shot error events when search fails.
     */
    public val errorEvents: LiveData<Event<Unit>> = _errorEvents

    /**
     * Coroutine scope tied to this [ViewModel].
     */
    private val scope = CoroutineScope(DispatcherProvider.Main)

    /**
     * Represents an ongoing search network request.
     */
    private var job: Job? = null

    private val logger by taggedLogger("Chat:SearchViewModel")

    /**
     * Changes the current query state. An empty search query
     */
    public fun setQuery(query: String) {
        job?.cancel()

        if (query.isEmpty()) {
            _state.value = State(
                query = query,
                results = emptyList(),
                canLoadMore = false,
                isLoading = false,
                isLoadingMore = false,
            )
        } else {
            _state.value = State(
                query = query,
                results = emptyList(),
                canLoadMore = true,
                isLoading = true,
                isLoadingMore = false,
            )
            searchMessages()
        }
    }

    /**
     * Loads more data when the user reaches the end of the found message list.
     *
     * Does nothing of the end of the search result list has already been reached or loading
     * is already in progress.
     */
    public fun loadMore() {
        job?.cancel()

        val currentState = _state.value!!
        if (currentState.canLoadMore && !currentState.isLoading && !currentState.isLoadingMore) {
            _state.value = currentState.copy(isLoadingMore = true)
            searchMessages()
        }
    }

    /**
     * Cancels the scope tied to this [ViewModel].
     */
    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    /**
     * Performs message search based on the current state.
     */
    private fun searchMessages() {
        job = scope.launch {
            val currentState = _state.value!!
            val result = searchMessages(
                query = currentState.query,
                next = currentState.next,
            )
            when (result) {
                is Result.Success -> handleSearchMessageSuccess(result.value)
                is Result.Failure -> handleSearchMessagesError(result.value)
            }
        }
    }

    /**
     * Notifies the UI about the search results and enables the pagination.
     */
    private suspend fun handleSearchMessageSuccess(searchResult: SearchMessagesResult) {
        logger.d { "Found messages: ${searchResult.messages.size}" }
        val currentState = _state.value!!
        val channels = chatClient.repositoryFacade.selectChannels(searchResult.messages.map { it.cid })
        _state.value = currentState.copy(
            results = currentState.results + searchResult.messages.map {
                MessageResult(
                    message = it,
                    channel = channels.firstOrNull { channel -> channel.cid == it.cid },
                )
            },
            isLoading = false,
            isLoadingMore = false,
            canLoadMore = !searchResult.next.isNullOrEmpty(),
            next = searchResult.next,
        )
    }

    /**
     * Notifies the UI about the error and enables the pagination.
     */
    private fun handleSearchMessagesError(streamError: Error) {
        logger.d { "Error searching messages: ${streamError.message}" }
        _state.value = _state.value!!.copy(
            isLoading = false,
            isLoadingMore = false,
        )
        _errorEvents.value = Event(Unit)
    }

    /**
     * Searches messages containing [query] text across channels where the current user is a member.
     *
     * @param query The search query.
     * @param next The cursor for the next page of results.
     */
    private suspend fun searchMessages(query: String, next: String?): Result<SearchMessagesResult> {
        logger.d { "Searching for \"$query\" with next: $next" }
        val currentUser = requireNotNull(chatClient.clientState.user.value)
        return chatClient
            .searchMessages(
                channelFilter = Filters.`in`("members", listOf(currentUser.id)),
                messageFilter = Filters.autocomplete("text", query),
                limit = QUERY_LIMIT,
                next = next,
            )
            .await()
    }

    /**
     * Represents the search screen state, used to render the required UI.
     *
     * @param query The current search query value.
     * @param results The found messages to render.
     * @param canLoadMore If we've reached the end of messages, to stop triggering pagination.
     * @param isLoading If we're currently loading data (initial load).
     * @param isLoadingMore If we're loading more items (pagination).
     * @param next The cursor for the next page of results.
     */
    public data class State(
        val query: String = "",
        val canLoadMore: Boolean = true,
        val results: List<MessageResult> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val next: String? = null,
    )

    private companion object {
        private const val QUERY_LIMIT = 30
    }
}
