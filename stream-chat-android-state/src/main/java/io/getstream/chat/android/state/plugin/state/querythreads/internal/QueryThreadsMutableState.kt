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

package io.getstream.chat.android.state.plugin.state.querythreads.internal

import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.state.plugin.state.querythreads.QueryThreadsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Mutable state holder of [QueryThreadsState] type.
 */
internal class QueryThreadsMutableState : QueryThreadsState {

    private var _threads: MutableStateFlow<List<Thread>>? = MutableStateFlow(emptyList())
    private var _loading: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loadingMore: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _next: MutableStateFlow<String?>? = MutableStateFlow(null)
    private var _unseenThreadIds: MutableStateFlow<Set<String>>? = MutableStateFlow(emptySet())

    // Note: The backing flow will always be initialized at this point
    override val threads: StateFlow<List<Thread>> = _threads!!
    override val loading: StateFlow<Boolean> = _loading!!
    override val loadingMore: StateFlow<Boolean> = _loadingMore!!
    override val next: StateFlow<String?> = _next!!
    override val unseenThreadIds: StateFlow<Set<String>> = _unseenThreadIds!!

    /**
     * Updates the loading state. Will be true only during the initial load, or during a full reload.
     *
     * @param loading The new loading state.
     */
    internal fun setLoading(loading: Boolean) {
        _loading?.value = loading
    }

    /**
     * Updates the loading more state. Will be true only during the loading of pages after the first one.
     *
     * @param loading The new loading more state.
     */
    internal fun setLoadingMore(loading: Boolean) {
        _loadingMore?.value = loading
    }

    /**
     * Updates the threads state with the new [threads] list. Overwrites the current threads list.
     *
     * @param threads The new threads state.
     */
    internal fun setThreads(threads: List<Thread>) {
        _threads?.value = threads
    }

    /**
     * Append the new page of [threads] to the current list of threads.
     *
     * @param threads The new page of threads.
     */
    internal fun appendThreads(threads: List<Thread>) {
        val currentThreads = _threads?.value.orEmpty()
        _threads?.value = currentThreads + threads
    }

    /**
     * Updates the identifier for the next page of threads.
     *
     * @param next The next page identifier.
     */
    internal fun setNext(next: String?) {
        _next?.value = next
    }

    /**
     * Adds a new thread to the set of unseen thread IDs.
     *
     * @param id The ID of the new [Thread].
     */
    internal fun addUnseenThreadId(id: String) {
        _unseenThreadIds?.update { set ->
            val mutableUnseenThreadIds = set.toMutableSet()
            mutableUnseenThreadIds.add(id)
            mutableUnseenThreadIds
        }
    }

    /**
     * Clears the set of unseen thread IDs.
     */
    internal fun clearUnseenThreadIds() {
        _unseenThreadIds?.value = emptySet()
    }

    /**
     * Clears all data from the state.
     */
    internal fun destroy() {
        _threads = null
        _loading = null
        _loadingMore = null
        _next = null
        _unseenThreadIds = null
    }
}
