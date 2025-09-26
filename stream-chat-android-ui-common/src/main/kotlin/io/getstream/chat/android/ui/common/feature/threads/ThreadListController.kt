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

package io.getstream.chat.android.ui.common.feature.threads

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.state.extensions.queryThreadsAsState
import io.getstream.chat.android.ui.common.state.threads.ThreadListState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * Controller responsible for managing the Threads list state. It serves as a central place for the state management and
 * business logic related to the Threads list.
 *
 * @param query The [QueryThreadsRequest] object containing the parameters for querying threads.
 * @param chatClient The [ChatClient] instance for retrieving the Threads related data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@InternalStreamChatApi
public class ThreadListController(
    private val query: QueryThreadsRequest,
    private val chatClient: ChatClient = ChatClient.instance(),
) {

    /**
     * Exposes the current thread list state.
     */
    private val _state: MutableStateFlow<ThreadListState> = MutableStateFlow(INITIAL_STATE)
    public val state: StateFlow<ThreadListState>
        get() = _state

    private val scope = chatClient.inheritScope { DispatcherProvider.IO }
    private val queryThreadsState = chatClient.queryThreadsAsState(
        request = query,
        coroutineScope = scope,
    )

    init {
        scope.launch {
            queryThreadsState
                .filterNotNull()
                .flatMapLatest {
                    combine(
                        it.threads,
                        it.loading,
                        it.loadingMore,
                        it.unseenThreadIds,
                    ) { threads, loading, loadingMore, unseenThreadIds ->
                        ThreadListState(threads, loading, loadingMore, unseenThreadIds.size)
                    }
                }
                .collectLatest {
                    _state.value = it
                }
        }
    }

    /**
     * Force loads the first page of threads.
     */
    public fun load() {
        chatClient.queryThreadsResult(query = query).enqueue()
    }

    /**
     * Loads the next page of threads (if possible).
     */
    public fun loadNextPage() {
        if (!shouldLoadNextPage()) return
        val next = queryThreadsState.value?.next?.value
        val nextPageQuery = query.copy(next = next)
        chatClient.queryThreadsResult(query = nextPageQuery).enqueue()
    }

    private fun shouldLoadNextPage(): Boolean {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingMore) {
            return false
        }
        // Load next page only if the 'next' param exists
        return queryThreadsState.value?.next?.value != null
    }

    public companion object {
        /**
         * Default value for the thread limit.
         */
        @InternalStreamChatApi
        public const val DEFAULT_THREAD_LIMIT: Int = 25

        /**
         * Default value for the thread reply limit.
         */
        @InternalStreamChatApi
        public const val DEFAULT_THREAD_REPLY_LIMIT: Int = 10

        /**
         * Default value for the thread participant limit.
         */
        @InternalStreamChatApi
        public const val DEFAULT_THREAD_PARTICIPANT_LIMIT: Int = 10

        private val INITIAL_STATE = ThreadListState(
            threads = emptyList(),
            isLoading = true,
            isLoadingMore = false,
            unseenThreadsCount = 0,
        )
    }
}
