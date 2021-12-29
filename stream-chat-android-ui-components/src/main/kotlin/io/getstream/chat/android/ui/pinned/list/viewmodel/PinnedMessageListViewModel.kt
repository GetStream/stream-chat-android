package io.getstream.chat.android.ui.pinned.list.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.utils.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Date

public class PinnedMessageListViewModel(private val cid: String) : ViewModel() {

    public data class State(
        val canLoadMore: Boolean,
        val results: List<Message>,
        val isLoading: Boolean,
        val nextDate: Date,
    )

    private companion object {
        private const val QUERY_LIMIT = 30

        val INITIAL_STATE = State(
            results = emptyList(),
            isLoading = false,
            canLoadMore = true,
            nextDate = Date(),
        )
    }

    private val scope = CoroutineScope(DispatcherProvider.Main + SupervisorJob())
    private val channelClient by lazy { ChatClient.instance().channel(cid) }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private val _state: MutableLiveData<State> = MutableLiveData(INITIAL_STATE)
    public val state: LiveData<State> = _state

    private val _errorEvents: MutableLiveData<Event<Unit>> = MutableLiveData()
    public val errorEvents: LiveData<Event<Unit>> = _errorEvents

    private val logger = ChatLogger.get("PinnedMessageListViewModel")

    init {
        scope.launch {
            _state.value = State(
                results = emptyList(),
                isLoading = true,
                canLoadMore = true,
                nextDate = Date(),
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

        logger.logD("Getting pinned messages (cid: $cid, before: ${currentState.nextDate}, limit: $QUERY_LIMIT)")

        val result = channelClient.getPinnedMessages(
            limit = QUERY_LIMIT,
            sort = QuerySort.desc(Message::pinnedAt),
            pagination = PinnedMessagesPagination.BeforeDate(
                date = currentState.nextDate,
                inclusive = false,
            )
        )
            .await()

        if (result.isSuccess) {
            val messages = result.data()
            logger.logD("Got ${messages.size} messages")
            _state.value = currentState.copy(
                results = currentState.results + messages,
                isLoading = false,
                canLoadMore = messages.size == QUERY_LIMIT,
                // Date() should only be assigned when messages are empty
                nextDate = messages.lastOrNull()?.pinnedAt ?: Date()
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
