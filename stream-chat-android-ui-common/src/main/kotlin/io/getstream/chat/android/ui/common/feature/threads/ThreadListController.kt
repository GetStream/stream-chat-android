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

package io.getstream.chat.android.ui.common.feature.threads

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.api.state.QueryThreadsState
import io.getstream.chat.android.client.api.state.queryThreadsAsState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.threads.ThreadListState
import io.getstream.chat.android.ui.common.utils.extensions.userSessionEnds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * Controller responsible for managing the Threads list state. It serves as a central place for the state management and
 * business logic related to the Threads list.
 *
 * @param query The [QueryThreadsRequest] object containing the parameters for querying threads.
 * @param chatClient The [ChatClient] instance for retrieving the Threads related data.
 * @param scope The [CoroutineScope] the controller's work runs under, in a child scope that [close] cancels; the given
 * scope itself is never cancelled. The default one outlives user sessions, so the list can follow the next user, and
 * runs one task at a time.
 * @param queryThreadsAsState Function that returns a [StateFlow] of [QueryThreadsState], built in the given scope.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@InternalStreamChatApi
public class ThreadListController(
    private val query: QueryThreadsRequest,
    private val chatClient: ChatClient = ChatClient.instance(),
    scope: CoroutineScope = CoroutineScope(DispatcherProvider.IO.limitedParallelism(1)),
    queryThreadsAsState: (CoroutineScope) -> StateFlow<QueryThreadsState?> = { stateScope ->
        chatClient.queryThreadsAsState(
            request = query,
            coroutineScope = stateScope,
        )
    },
) : AutoCloseable {

    private val ownScope = CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job]))

    /**
     * Exposes the current thread list state.
     */
    private val _state: MutableStateFlow<ThreadListState> = MutableStateFlow(INITIAL_STATE)
    public val state: StateFlow<ThreadListState>
        get() = _state

    /**
     * The state of our network connection - if we're online, connecting or offline.
     */
    public val connectionState: StateFlow<ConnectionState> = chatClient.clientState.connectionState

    /**
     * The state of the currently logged in user.
     */
    public val user: StateFlow<User?> = chatClient.clientState.user

    private val queryThreadsState = queryThreadsAsState(ownScope)

    init {
        ownScope.launch {
            queryThreadsState
                // Null while no user is connected: stop following the previous state.
                .flatMapLatest { state ->
                    state?.let {
                        combine(
                            it.threads,
                            it.loading,
                            it.loadingMore,
                            it.unseenThreadIds,
                            it.loadingError,
                        ) { threads, loading, loadingMore, unseenThreadIds, loadingError ->
                            ThreadListState(threads, loading, loadingMore, unseenThreadIds.size, loadingError)
                        }
                    } ?: emptyFlow()
                }
                .collectLatest {
                    _state.value = it
                }
        }
        ownScope.launch {
            // Drops the previous user's threads; the query runs again once the next user is connected.
            chatClient.clientState.userSessionEnds().collect { _state.value = INITIAL_STATE }
        }
    }

    /**
     * Cancels the work started by this controller, without cancelling the scope it was given.
     */
    override fun close() {
        ownScope.cancel()
    }

    /**
     * Force loads the first page of threads.
     */
    public fun load() {
        chatClient.queryThreads(query = query).enqueue()
    }

    /**
     * Loads the next page of threads (if possible).
     */
    public fun loadNextPage() {
        val nextPage = getNextPage() ?: return
        val nextPageQuery = query.copy(next = nextPage)
        chatClient.queryThreads(query = nextPageQuery).enqueue()
    }

    private fun getNextPage(): String? {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingMore) {
            return null
        }
        return queryThreadsState.value?.next?.value
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
            loadingError = false,
        )
    }
}
