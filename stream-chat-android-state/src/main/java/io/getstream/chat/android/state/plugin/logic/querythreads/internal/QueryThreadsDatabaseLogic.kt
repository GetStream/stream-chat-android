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

package io.getstream.chat.android.state.plugin.logic.querythreads.internal

import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.models.Thread

/**
 * Class providing offline support for the 'Query Threads' operation.
 *
 * @param repository Implementation of [ThreadsRepository] for accessing thr local data.
 */
internal class QueryThreadsDatabaseLogic(
    private val repository: ThreadsRepository,
) {

    companion object {
        private const val LOCAL_THREAD_ORDER_ID = "localThreadOrderId"
    }

    /**
     * Retrieves the order in which the local threads should be shown to the user.
     */
    internal suspend fun getLocalThreadsOrder(): List<String> {
        return repository.selectThreadOrder(LOCAL_THREAD_ORDER_ID)
    }

    /**
     * Inserts the order in which the local threads should be shown to the user.
     */
    internal suspend fun setLocalThreadsOrder(order: List<String>) {
        repository.insertThreadOrder(LOCAL_THREAD_ORDER_ID, order)
    }

    /**
     * Retrieves the local threads in the order specified by [ids].
     */
    internal suspend fun getLocalThreads(ids: List<String>): List<Thread> {
        return repository.selectThreads(ids)
    }
}
