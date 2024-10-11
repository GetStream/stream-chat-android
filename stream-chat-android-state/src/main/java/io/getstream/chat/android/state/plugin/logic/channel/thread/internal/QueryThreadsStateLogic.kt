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

package io.getstream.chat.android.state.plugin.logic.channel.thread.internal

import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.state.plugin.state.querythreads.internal.QueryThreadsMutableState

/**
 * Logic for managing the state of the threads list.
 *
 * @param mutableState Reference to the global [QueryThreadsMutableState].
 */
internal class QueryThreadsStateLogic(private val mutableState: QueryThreadsMutableState) {

    /**
     * Updates the loading state of the [mutableState].
     *
     * @param loading The new loading state.
     */
    internal fun setLoading(loading: Boolean) =
        mutableState.setLoading(loading)

    /**
     * Updates the loading more state of the [mutableState].
     *
     * @param loading The new loading more state.
     */
    internal fun setLoadingMore(loading: Boolean) =
        mutableState.setLoadingMore(loading)

    /**
     * Updates the thread state of the [mutableState].
     *
     * @param threads The new threads state.
     */
    internal fun setThreads(threads: List<Thread>) =
        mutableState.setThreads(threads)

    /**
     * Appends the new page of [threads] to the current thread list.
     *
     * @param threads The new page of threads.
     */
    internal fun appendThreads(threads: List<Thread>) =
        mutableState.appendThreads(threads)

    /**
     * Updates the end of threads reached indicator in the [mutableState].
     *
     * @param endOfThreadsReached The new end of threads indicator.
     */
    internal fun setEndOfThreadsReached(endOfThreadsReached: Boolean) =
        mutableState.setEndOfThreadsReached(endOfThreadsReached)
}
