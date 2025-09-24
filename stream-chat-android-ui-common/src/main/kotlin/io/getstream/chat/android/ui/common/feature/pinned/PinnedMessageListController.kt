/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.pinned

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.PinnedMessagesPagination
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.pinned.PinnedMessageListState
import io.getstream.log.TaggedLogger
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller responsible for handling pinned message list state. It acts as a central place for business logic and
 * state management required to show the pinned messages in a channel.
 * The pinned messages are presented in a descending order based on [Message.pinnedAt].
 *
 * @param cid The full channel ID. Ex: "messaging:123".
 * @param channelClient The ChannelClient (instantiated for the given [cid]).
 */
@InternalStreamChatApi
public class PinnedMessageListController(
    private val cid: String,
    private val channelClient: ChannelClient = ChatClient.instance().channel(cid),
) {

    private companion object {

        private const val QUERY_LIMIT = 30
    }

    /**
     * Exposes the current pinned messages list state.
     */
    private val _state: MutableStateFlow<PinnedMessageListState> = MutableStateFlow(PinnedMessageListState())
    public val state: StateFlow<PinnedMessageListState>
        get() = _state

    /**
     * Emits one-shot error events when the loading of pinned messages fails.
     */
    private val _errorEvents: MutableSharedFlow<Unit> = MutableSharedFlow()
    public val errorEvents: SharedFlow<Unit>
        get() = _errorEvents

    private val logger: TaggedLogger by taggedLogger("PinnedMessageListController")
    private val scope = CoroutineScope(DispatcherProvider.Main + SupervisorJob())

    /**
     * Loads the initial list of pinned messages.
     */
    public fun load() {
        scope.launch {
            // Ensure the state is updated with the current date(timestamp) for initial loading
            _state.value = PinnedMessageListState()
            loadPinnedMessages()
        }
    }

    /**
     * Loads more pinned messages when requested.
     * If the end of the list has been reached, or loading is already in progress, the request will be ignored.
     */
    public fun loadMore() {
        scope.launch {
            if (!shouldLoadMore()) {
                return@launch
            }
            // In the case where we are loading more messages, we append a new empty message to indicate pagination.
            _state.update { current ->
                current.copy(
                    isLoading = true,
                    results = current.results + MessageResult(Message(), null),
                )
            }
            loadPinnedMessages()
        }
    }

    private suspend fun loadPinnedMessages() {
        val nextDate = _state.value.nextDate
        logger.d { "Loading pinned messages (cid: $cid, before: $nextDate, limit: $QUERY_LIMIT)" }
        val result = channelClient.getPinnedMessages(
            limit = QUERY_LIMIT,
            sort = QuerySortByField.descByName("pinned_at"),
            pagination = PinnedMessagesPagination.BeforeDate(nextDate, inclusive = false),
        ).await()
        when (result) {
            is Result.Success -> {
                val messages = result.value
                logger.d { "Loaded ${messages.size} pinned messages" }
                _state.update { current ->
                    current.copy(
                        results = (
                            current.results + messages.map { message ->
                                MessageResult(
                                    message,
                                    null,
                                )
                            }
                            ).filter { it.message.id.isNotEmpty() },
                        isLoading = false,
                        canLoadMore = messages.size == QUERY_LIMIT,
                        nextDate = messages.lastOrNull()?.pinnedAt ?: nextDate,
                    )
                }
            }

            is Result.Failure -> {
                logger.d { "Loading pinned messages failed: ${result.value.message}" }
                _state.update { current ->
                    current.copy(isLoading = false, canLoadMore = true)
                }
                _errorEvents.emit(Unit)
            }
        }
    }

    private fun shouldLoadMore(): Boolean {
        val currentState = _state.value
        return when {
            !currentState.canLoadMore -> {
                logger.d { "No more messages to load" }
                false
            }

            currentState.isLoading -> {
                logger.d { "Already loading" }
                false
            }

            else -> true
        }
    }
}
