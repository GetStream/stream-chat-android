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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.plugin.listeners.QueryThreadsListener
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.result.Result

/**
 * [QueryThreadsListener] implementation for the [StatePlugin].
 * Ensures that the "Query Threads" state is properly populated by using the [LogicRegistry.threads].
 *
 * @param logic The [LogicRegistry] providing the business logic.
 */
internal class QueryThreadsListenerState(private val logic: LogicRegistry) : QueryThreadsListener {

    override suspend fun onQueryThreadsPrecondition(request: QueryThreadsRequest): Result<Unit> {
        return logic.threads(request).onQueryThreadsPrecondition(request)
    }

    override suspend fun onQueryThreadsRequest(request: QueryThreadsRequest) {
        logic.threads(request).onQueryThreadsRequest(request)
    }

    override suspend fun onQueryThreadsResult(result: Result<QueryThreadsResult>, request: QueryThreadsRequest) {
        logic.threads(request).onQueryThreadsResult(result, request)
    }
}
