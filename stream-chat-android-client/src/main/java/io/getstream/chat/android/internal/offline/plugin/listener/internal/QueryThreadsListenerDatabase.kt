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

package io.getstream.chat.android.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.client.plugin.listeners.QueryThreadsListener
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.result.Result

/**
 * [QueryThreadsListener] implementation for the [OfflinePlugin].
 * Ensures that the newly fetched threads are persisted in the database.
 *
 * @param threadsRepository The [ThreadsRepository] for accessing the database.
 */
internal class QueryThreadsListenerDatabase(
    private val threadsRepository: ThreadsRepository,
) : QueryThreadsListener {

    override suspend fun onQueryThreadsPrecondition(request: QueryThreadsRequest): Result<Unit> = Result.Success(Unit)

    override suspend fun onQueryThreadsRequest(request: QueryThreadsRequest) {
        /* No-Op */
    }

    override suspend fun onQueryThreadsResult(result: Result<QueryThreadsResult>, request: QueryThreadsRequest) {
        if (result is Result.Success) {
            // Store all threads
            threadsRepository.insertThreads(result.value.threads)
        }
    }
}
