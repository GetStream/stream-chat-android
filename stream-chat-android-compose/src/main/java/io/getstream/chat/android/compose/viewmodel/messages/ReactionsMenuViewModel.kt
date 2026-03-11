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

package io.getstream.chat.android.compose.viewmodel.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.querysort.QuerySortByField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for loading reactions for a message with cursor-based pagination.
 *
 * @param messageId The ID of the message to fetch reactions for.
 * @param chatClient The [ChatClient] instance used for querying reactions.
 */
internal class ReactionsMenuViewModel(
    private val messageId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val _state = MutableStateFlow(ReactionsMenuState())
    val state: StateFlow<ReactionsMenuState> = _state.asStateFlow()

    /** Cached pages per reaction type filter (null key = all reactions). */
    private val cache = mutableMapOf<String?, ReactionPages>()

    init {
        viewModelScope.launch {
            load()
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun selectReaction(type: String?) {
        val newType = if (_state.value.selectedReactionType == type) null else type
        val cached = cache[newType]
        if (cached != null) {
            _state.update {
                it.copy(
                    selectedReactionType = newType,
                    reactions = cached.reactions,
                    isLoading = false,
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedReactionType = newType,
                    reactions = emptyList(),
                    isLoading = true,
                )
            }
            viewModelScope.launch {
                load()
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        val pages = cache[current.selectedReactionType] ?: return
        if (!pages.canLoadMore || current.isLoading || current.isLoadingMore) return
        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            load()
            _state.update { it.copy(isLoadingMore = false) }
        }
    }

    private suspend fun load() {
        val type = _state.value.selectedReactionType
        val pages = cache[type]
        val filter = type?.let { Filters.eq("type", it) }
        chatClient.queryReactions(
            messageId = messageId,
            filter = filter,
            limit = QueryLimit,
            next = pages?.nextCursor,
            sort = QuerySortByField.descByName("created_at"),
        ).await()
            .onSuccess { result ->
                val updated = ReactionPages(
                    reactions = (pages?.reactions.orEmpty()) + result.reactions,
                    nextCursor = result.next,
                    canLoadMore = result.next != null,
                )
                cache[type] = updated
                _state.update { it.copy(reactions = updated.reactions) }
            }
    }

    private data class ReactionPages(
        val reactions: List<Reaction> = emptyList(),
        val nextCursor: String? = null,
        val canLoadMore: Boolean = true,
    )
}

internal data class ReactionsMenuState(
    val reactions: List<Reaction> = emptyList(),
    val selectedReactionType: String? = null,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
)

private const val QueryLimit = 25
