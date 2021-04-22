package io.getstream.chat.android.ui.search.list.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.SearchMessagesRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.utils.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

public class SearchViewModel : ViewModel() {

    public data class State(
        val query: String,
        val canLoadMore: Boolean,
        val results: List<Message>,
        val isLoading: Boolean,
    )

    private companion object {
        private const val QUERY_LIMIT = 30

        val INITIAL_STATE = State(
            query = "",
            results = emptyList(),
            isLoading = false,
            canLoadMore = true,
        )
    }

    private val scope = CoroutineScope(DispatcherProvider.Main + SupervisorJob())

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private val _state: MutableLiveData<State> = MutableLiveData(INITIAL_STATE)
    public val state: LiveData<State> = _state

    private val _errorEvents: MutableLiveData<Event<Unit>> = MutableLiveData()
    public val errorEvents: LiveData<Event<Unit>> = _errorEvents

    private val logger = ChatLogger.get("SearchViewModel")

    public fun setQuery(query: String) {
        if (query.isEmpty()) {
            _state.value = State(
                query = query,
                canLoadMore = false,
                results = emptyList(),
                isLoading = false
            )
            return
        }

        scope.launch {
            _state.value = State(
                query = query,
                results = emptyList(),
                isLoading = true,
                canLoadMore = true,
            )
            fetchServerResults()
        }
    }

    public fun loadMore() {
        scope.launch {
            val currentState = _state.value!!

            if (!currentState.canLoadMore) {
                logger.logD("No more messages to load")
                return@launch
            }
            if (currentState.isLoading) {
                logger.logD("Already loading")
                return@launch
            }

            _state.value = currentState.copy(
                isLoading = true,
            )
            fetchServerResults()
        }
    }

    private suspend fun fetchServerResults() {
        val currentState = _state.value!!
        val currentUser = requireNotNull(ChatClient.instance().getCurrentUser())
        val channelFilter = Filters.`in`("members", listOf(currentUser.id))
        val messageFilter = Filters.autocomplete("text", currentState.query)

        val request = SearchMessagesRequest(
            offset = currentState.results.size,
            limit = QUERY_LIMIT,
            channelFilter = channelFilter,
            messageFilter = messageFilter,
        )

        logger.logD("Searching (offset: ${request.offset}, limit: ${request.limit}, query: ${currentState.query})")

        val result = ChatClient.instance()
            .searchMessages(request)
            .await()

        if (result.isSuccess) {
            logger.logD("Got ${result.data().size} messages")
            _state.value = currentState.copy(
                results = currentState.results + result.data(),
                isLoading = false,
                canLoadMore = result.data().size == QUERY_LIMIT
            )
        } else {
            logger.logD("Error ${result.error().message}")
            _state.value = currentState.copy(
                isLoading = false,
                canLoadMore = true,
            )
            _errorEvents.setValue(Event(Unit))
        }
    }
}
