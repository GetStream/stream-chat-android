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

package io.getstream.chat.android.ui.common.feature.channel.attachments

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState
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

@InternalStreamChatApi
public class ChannelAttachmentsViewController(
    scope: CoroutineScope,
    private val cid: String,
    private val attachmentType: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) {

    private val logger by taggedLogger("Chat:ChannelAttachmentsViewController")

    /**
     * This flow is used to trigger the loading of attachments when needed.
     */
    private val loadRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow<ChannelAttachmentsViewState>(ChannelAttachmentsViewState.Loading)

    /**
     * The current state of the channel attachments view.
     */
    public val state: StateFlow<ChannelAttachmentsViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChannelAttachmentsViewEvent>(extraBufferCapacity = 1)

    /**
     * One shot events triggered by the controller.
     */
    public val events: SharedFlow<ChannelAttachmentsViewEvent> = _events.asSharedFlow()

    init {
        loadRequests.onStart { emit(Unit) } // Triggers the initial load
            .flatMapLatest { flowOf(searchAttachments()) }
            .onEach { result ->
                result
                    .onSuccess { onSuccessResult(it) }
                    .onError { onFailureResult(it) }
            }
            .launchIn(scope)
    }

    public fun onViewAction(action: ChannelAttachmentsViewAction) {
        when (action) {
            ChannelAttachmentsViewAction.LoadMoreRequested -> loadMore()
        }
    }

    private suspend fun searchAttachments(): Result<SearchMessagesResult> {
        val (channelType, channelId) = cid.cidToTypeAndId()
        val channelFilter = Filters.`in`("cid", "$channelType:$channelId")
        val messageFilter = Filters.`in`("attachments.type", listOf(attachmentType))
        val nextPage = (_state.value as? ChannelAttachmentsViewState.Content)?.nextPage
        logger.d {
            "[searchAttachments] filter: $channelFilter and $messageFilter, limit: $QUERY_LIMIT, nextPage: $nextPage"
        }
        return chatClient.searchMessages(
            channelFilter = channelFilter,
            messageFilter = messageFilter,
            limit = QUERY_LIMIT,
            next = nextPage,
        ).await()
    }

    private fun onSuccessResult(result: SearchMessagesResult) {
        val messages = result.messages
        val next = result.next
        logger.d { "[onSuccessResult] messages: ${messages.size}, next: $next" }
        val attachments = result.messages.flatMap { message ->
            message.attachments.filter { it.type == attachmentType }
        }
        _state.update { currentState ->
            val currentResults = if (currentState is ChannelAttachmentsViewState.Content) {
                currentState.results
            } else {
                emptyList()
            }
            ChannelAttachmentsViewState.Content(
                results = currentResults + attachments,
                nextPage = next,
                canLoadMore = next != null,
                isLoadingMore = false,
            )
        }
    }

    private fun onFailureResult(error: Error) {
        logger.e { "[onFailureResult] error: ${error.message}" }
        _state.update { currentState ->
            when (currentState) {
                is ChannelAttachmentsViewState.Loading -> ChannelAttachmentsViewState.Error(message = error.message)
                is ChannelAttachmentsViewState.Content -> currentState.copy(isLoadingMore = false)
                else -> currentState
            }
        }
        _events.tryEmit(ChannelAttachmentsViewEvent.Error(message = error.message))
    }

    private fun loadMore() {
        when (val currentState = state.value) {
            is ChannelAttachmentsViewState.Content -> {
                if (!currentState.canLoadMore) {
                    logger.d { "[loadMore] no more attachments to load" }
                    return
                }

                if (currentState.isLoadingMore) {
                    logger.d { "[loadMore] already loading more attachments" }
                    return
                }

                logger.d { "[loadMore] no args" }
                _state.value = currentState.copy(isLoadingMore = true)
                loadRequests.tryEmit(Unit)
            }

            else -> {
                logger.d { "[loadMore] current state is not Content, cannot load more attachments" }
            }
        }
    }
}

private const val QUERY_LIMIT = 30
