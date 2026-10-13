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

package io.getstream.chat.android.ai.compose.sample.presentation.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.queryChannelsAsState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.log.taggedLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing conversation list and selection.
 * Handles loading conversations from Stream Chat and managing conversation selection.
 *
 * @param chatClient The Stream Chat client instance
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class ConversationListViewModel(
    private val chatClient: ChatClient,
) : ViewModel() {

    private val logger by taggedLogger()

    private val _uiState = MutableStateFlow(ConversationListState())
    val uiState: StateFlow<ConversationListState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        val currentUserId = chatClient.getCurrentUser()?.id ?: run {
            logger.d { "No current user, cannot load channels" }
            return
        }

        val request = QueryChannelsRequest(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(currentUserId)),
                Filters.or(
                    Filters.notExists("draft"),
                    Filters.eq("draft", false),
                ),
            ),
            querySort = QuerySortByField.descByName("last_updated"),
            limit = 30,
            messageLimit = 1,
        )

        chatClient.queryChannelsAsState(request = request, coroutineScope = viewModelScope)
            .filterNotNull()
            .flatMapLatest { queryChannelsState -> queryChannelsState.channels.filterNotNull() }
            .onEach { channels ->
                logger.d { "Loaded ${channels.size} channels from Stream Chat" }

                val conversations = channels.map(Channel::toConversation)

                _uiState.update { state ->
                    state.copy(
                        conversations = conversations,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}

private fun Channel.toConversation(): Conversation {
    val title = name.takeIf(String::isNotBlank) ?: "Unnamed Chat"

    return Conversation(
        id = cid,
        title = title,
    )
}
