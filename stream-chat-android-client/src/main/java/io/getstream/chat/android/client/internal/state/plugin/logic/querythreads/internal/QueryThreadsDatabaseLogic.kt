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

package io.getstream.chat.android.client.internal.state.plugin.logic.querythreads.internal

import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Class providing offline support for the 'Query Threads' operation.
 *
 * @param repository Implementation of [ThreadsRepository] for accessing thr local data.
 */
internal class QueryThreadsDatabaseLogic(
    private val repository: ThreadsRepository,
) {

    /**
     * Retrieves the order in which the local threads should be shown to the user based on the provided [filter] and
     * [sort].
     *
     * @param filter The filter used in the query.
     * @param sort The sorting criteria used in the query.
     * @return A list of thread IDs representing the order in which threads should be displayed.
     */
    internal suspend fun getLocalThreadsOrder(filter: FilterObject?, sort: QuerySorter<Thread>): List<String> {
        val queryId = queryId(filter, sort)
        return repository.selectThreadOrder(queryId)
    }

    /**
     * Inserts the order in which the local threads should be shown to the user for the given query ([filter] and
     * [sort]).
     *
     * @param filter The filter used in the query.
     * @param sort The sorting criteria used in the query.
     * @param order A list of thread IDs representing the order in which threads should be displayed.
     */
    internal suspend fun setLocalThreadsOrder(filter: FilterObject?, sort: QuerySorter<Thread>, order: List<String>) {
        val queryId = queryId(filter, sort)
        repository.insertThreadOrder(queryId, order)
    }

    /**
     * Retrieves the local threads in the order specified by [ids].
     */
    internal suspend fun getLocalThreads(ids: List<String>): List<Thread> {
        return repository.selectThreads(ids)
    }

    private fun queryId(filter: FilterObject?, sort: QuerySorter<Thread>): String {
        return "${filter.hashCode()}-${sort.toDto().hashCode()}"
    }
}
