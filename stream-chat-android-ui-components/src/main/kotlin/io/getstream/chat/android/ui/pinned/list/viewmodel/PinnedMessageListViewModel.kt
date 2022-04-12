/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

/**
 * ViewModel responsible for providing pinned messages in the channel.
 * Pinned messages are provided in a descending order based on [Message.pinnedAt].
 * Can be bound to the view using [PinnedMessageListViewModel.bindView] function.
 *
 * @param cid The full channel id. ie messaging:123.
 */
public class PinnedMessageListViewModel(private val cid: String) : ViewModel() {

    /**
     * Represents the pinned messages state, used to render the required UI.
     *
     * @param canLoadMore If we've reached the end of messages, to stop triggering pagination.
     * @param results The messages to render.
     * @param isLoading If we're currently loading data (initial load).
     * @param nextDate Date used to fetch next page of the messages.
     */
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

    /**
     * Called when [PinnedMessageListViewModel] is no longer used and will be destroyed.
     * Calls super and cancels the scope tied to this [ViewModel].
     */
    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private val _state: MutableLiveData<State> = MutableLiveData(INITIAL_STATE)

    /**
     * The current pinned messages' state.
     */
    public val state: LiveData<State> = _state

    private val _errorEvents: MutableLiveData<Event<Unit>> = MutableLiveData()

    /**
     * One shot error events when query fails.
     */
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

    /**
     * Loads more data when requested.
     *
     * Does nothing if the end of the list has already been reached or loading is already in progress.
     */
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

    /**
     * Fetches pinned messages based on the current state.
     */
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
                // currentState.nextDate should only be assigned when messages are empty
                nextDate = messages.lastOrNull()?.pinnedAt ?: currentState.nextDate
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
