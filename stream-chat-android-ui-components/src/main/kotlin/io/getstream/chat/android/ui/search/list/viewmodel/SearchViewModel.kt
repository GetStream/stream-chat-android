package io.getstream.chat.android.ui.search.list.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.utils.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import io.getstream.chat.android.client.utils.Result

/**
 * ViewModel responsible for searching for messages that match a particular search query.
 */
public class SearchViewModel : ViewModel() {

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

    private val logger = ChatLogger.get("SearchViewModel")

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
                offset = currentState.results.size
            )
            if (result.isSuccess) {
                handleSearchMessageSuccess(result.data())
            } else {
                handleSearchMessagesError(result.error())
            }
        }
    }

    /**
     * Notifies the UI about the search results and enables the pagination.
     */
    private fun handleSearchMessageSuccess(messages: List<Message>) {
        logger.logD("Found messages: ${messages.size}")
        val currentState = _state.value!!
        _state.value = currentState.copy(
            results = currentState.results + messages,
            isLoading = false,
            isLoadingMore = false,
            canLoadMore = messages.size == QUERY_LIMIT
        )
    }

    /**
     * Notifies the UI about the error and enables the pagination.
     */
    private fun handleSearchMessagesError(chatError: ChatError) {
        logger.logD("Error searching messages: ${chatError.message}")
        _state.value = _state.value!!.copy(
            isLoading = false,
            isLoadingMore = false,
            canLoadMore = true,
        )
        _errorEvents.value = Event(Unit)
    }

    /**
     * Searches messages containing [query] text across channels where the current user is a member.
     *
     * @param query The search query.
     * @param offset The pagination offset offset.
     */
    private suspend fun searchMessages(query: String, offset: Int): Result<List<Message>> {
        logger.logD("Searching for \"$query\" with offset: $offset")
        val currentUser = requireNotNull(ChatClient.instance().getCurrentUser())
        // TODO: use the pagination based on "limit" nad "next" params here
        return ChatClient.instance()
            .searchMessages(
                channelFilter = Filters.`in`("members", listOf(currentUser.id)),
                messageFilter = Filters.autocomplete("text", query),
                offset = offset,
                limit = QUERY_LIMIT,
            )
            .await()
            .map { it.messages }
    }

    /**
     * Represents the search screen state, used to render the required UI.
     *
     * @param query The current search query value.
     * @param results The found messages to render.
     * @param canLoadMore If we've reached the end of messages, to stop triggering pagination.
     * @param isLoading If we're currently loading data (initial load).
     * @param isLoadingMore If we're loading more items (pagination).
     */
    public data class State(
        val query: String = "",
        val canLoadMore: Boolean = true,
        val results: List<Message> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
    )

    private companion object {
        private const val QUERY_LIMIT = 30
    }
}
