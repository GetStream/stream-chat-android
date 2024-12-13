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

package io.getstream.chat.android.ui.viewmodel.mentions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.state.utils.Event
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

public class MentionListViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    public data class State(
        val canLoadMore: Boolean,
        val results: List<MessageResult>,
        val isLoading: Boolean,
    )

    private companion object {
        private const val QUERY_LIMIT = 30

        val INITIAL_STATE = State(
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

    private val logger by taggedLogger("Chat:MentionListViewModel")

    init {
        scope.launch {
            _state.value = State(
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
                logger.d { "No more messages to load" }
                return@launch
            }
            if (currentState.isLoading) {
                logger.d { "Already loading" }
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
        val currentUser = requireNotNull(chatClient.clientState.user.value)
        val channelFilter = Filters.`in`("members", listOf(currentUser.id))
        val messageFilter = Filters.contains("mentioned_users.id", currentUser.id)

        logger.d {
            "Getting mentions (offset: ${currentState.results.size}, limit: $QUERY_LIMIT, user ID: ${currentUser.id})"
        }

        val result = chatClient
            .searchMessages(
                channelFilter = channelFilter,
                messageFilter = messageFilter,
                offset = currentState.results.size,
                limit = QUERY_LIMIT,
            )
            .await()

        when (result) {
            is Result.Success -> {
                val messages = result.value.messages
                val channels = chatClient.repositoryFacade.selectChannels(messages.map { it.cid })
                logger.d { "Got ${messages.size} messages" }
                _state.value = currentState.copy(
                    results = currentState.results + messages.map { message ->
                        MessageResult(
                            message = message,
                            channel = channels.firstOrNull { it.cid == message.cid },
                        )
                    },
                    isLoading = false,
                    canLoadMore = messages.size == QUERY_LIMIT,
                )
            }
            is Result.Failure -> {
                logger.d { "Error ${result.value.message}" }
                _state.value = currentState.copy(
                    isLoading = false,
                    canLoadMore = true,
                )
                _errorEvents.setValue(Event(Unit))
            }
        }
    }
}
