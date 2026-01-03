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

package io.getstream.chat.android.client.persistance.repository.noop

import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread

/**
 * No-Op implementation of the [ThreadsRepository].
 */
internal object NoOpThreadsRepository : ThreadsRepository {
    override suspend fun insertThreadOrder(id: String, order: List<String>) {
        /* No-Op */
    }

    override suspend fun selectThreadOrder(id: String): List<String> = emptyList()
    override suspend fun insertThreads(threads: List<Thread>) {
        /* No-Op */
    }

    override suspend fun upsertMessageInThread(message: Message) {
        /* No-Op */
    }

    override suspend fun upsertMessagesInThread(messages: List<Message>) {
        /* No-Op */
    }

    override suspend fun selectThread(id: String): Thread? = null
    override suspend fun selectThreads(ids: List<String>): List<Thread> = emptyList()
    override suspend fun deleteChannelThreads(cid: String) {
        /* No-Op */
    }

    override suspend fun clear() {
        /* No-Op */
    }
}
