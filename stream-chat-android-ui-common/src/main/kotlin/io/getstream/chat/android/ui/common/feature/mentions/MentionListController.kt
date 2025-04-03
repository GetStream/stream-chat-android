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

package io.getstream.chat.android.ui.common.feature.mentions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.mentions.MentionListEvent
import io.getstream.chat.android.ui.common.state.mentions.MentionListState
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
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
 * Controller responsible for managing the state of the mention list.
 *
 * @param scope The [CoroutineScope] on which coroutines should be launched.
 * @param sort The sorting options for the messages.
 * @param chatClient The ChatClient instance to use for API calls.
 */
@InternalStreamChatApi
public class MentionListController(
    scope: CoroutineScope,
    private val sort: QuerySorter<Message>?,
    private val chatClient: ChatClient = ChatClient.instance(),
) {
    private val logger by taggedLogger("Chat:MentionListController")

    private val loadRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow(MentionListState())

    /**
     * The current state of the mention list.
     */
    public val state: StateFlow<MentionListState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MentionListEvent>(extraBufferCapacity = 1)

    /**
     * One shot events triggered by the controller.
     */
    public val events: SharedFlow<MentionListEvent> = _events.asSharedFlow()

    init {
        loadRequests.onStart { emit(Unit) } // Triggers the initial load
            .flatMapLatest { flowOf(searchMentions()) }
            .onEach { result ->
                when (result) {
                    is Result.Success -> onSuccessResult(result.value)
                    is Result.Failure -> onFailureResult(result.value)
                }
            }
            .launchIn(scope)
    }

    /**
     * Loads more mentions if there are more to load.
     */
    public fun loadMore() {
        val currentState = state.value

        if (!currentState.canLoadMore) {
            logger.d { "[loadMore] no more mentions to load" }
            return
        }

        if (currentState.isLoadingMore) {
            logger.d { "[loadMore] already loading more mentions" }
            return
        }

        logger.d { "[loadMore] no args" }
        _state.value = currentState.copy(isLoadingMore = true)
        loadRequests.tryEmit(Unit)
    }

    public fun refresh() {
        logger.d { "[refresh] no args" }
        _state.value = InitialState
        loadRequests.tryEmit(Unit)
    }

    private suspend fun searchMentions(): Result<SearchMessagesResult> {
        val currentUser = requireNotNull(chatClient.getCurrentUser())
        val channelFilter = Filters.`in`("members", listOf(currentUser.id))
        val messageFilter = Filters.contains("mentioned_users.id", currentUser.id)
        val currentState = _state.value
        logger.d {
            "[searchMentions] currentUser: ${currentUser.id}, limit: $QUERY_LIMIT, nextPage: ${currentState.nextPage}"
        }
        return chatClient.searchMessages(
            channelFilter = channelFilter,
            messageFilter = messageFilter,
            limit = QUERY_LIMIT,
            next = currentState.nextPage,
            sort = sort,
        ).await()
    }

    private suspend fun onSuccessResult(result: SearchMessagesResult) {
        val messages = result.messages
        val next = result.next
        logger.d { "[onSuccessResult] messages: ${messages.size}, next: $next" }
        val channels = chatClient.repositoryFacade.selectChannels(messages.map(Message::cid))
        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                results = currentState.results + messages.map { message ->
                    MessageResult(
                        message = message,
                        channel = channels.firstOrNull { channel -> channel.cid == message.cid },
                    )
                },
                nextPage = next,
                canLoadMore = next != null,
                isLoadingMore = false,
            )
        }
    }

    private fun onFailureResult(error: Error) {
        logger.e { "[onFailureResult] error: ${error.message}" }
        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                isLoadingMore = false,
            )
        }
        _events.tryEmit(MentionListEvent.Error(message = error.message))
    }
}

private const val QUERY_LIMIT = 30
