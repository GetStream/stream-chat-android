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

import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.result.Result

/**
 * Logic class for "Query Threads" operations.
 *
 * @param stateLogic The [QueryThreadsStateLogic] managing the global state of the threads list.
 */
internal class QueryThreadsLogic(private val stateLogic: QueryThreadsStateLogic) {

    /**
     * Handles the actions that are needed to update the threads state before the attempt to load the threads
     * from the network.
     *
     * @param request The [QueryThreadsRequest] used to fetch the threads.
     */
    fun onQueryThreadsRequest(request: QueryThreadsRequest) {
        if (isLoadingMore(request)) {
            stateLogic.setLoadingMore(true)
        } else {
            stateLogic.setLoading(true)
        }
    }

    /**
     * Handles the actions that are needed to update the threads state after the loading of the threads from network was
     * completed.
     *
     * @param result The [Result] holding the [QueryThreadsResult] if the operation was successful, or an [Error] if the
     * operation failed.
     * @param request The [QueryThreadsRequest] used to fetch the threads.
     */
    fun onQueryThreadsResult(result: Result<QueryThreadsResult>, request: QueryThreadsRequest) {
        val isLoadingMore = isLoadingMore(request)
        if (isLoadingMore) {
            stateLogic.setLoadingMore(false)
        } else {
            stateLogic.setLoading(false)
        }
        when (result) {
            is Result.Success -> {
                if (isLoadingMore) {
                    stateLogic.appendThreads(result.value.threads)
                } else {
                    stateLogic.setThreads(result.value.threads)
                }
                val isEndReached = isEndReached(result.value)
                stateLogic.setEndOfThreadsReached(isEndReached)
            }

            is Result.Failure -> {
                // TODO: What to do in this case??
            }
        }
    }

    private fun isLoadingMore(request: QueryThreadsRequest) = request.next != null

    private fun isEndReached(result: QueryThreadsResult) = result.next == null
}
