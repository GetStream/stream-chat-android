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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread

/**
 * Repository for read/write operations related to threads.
 */
public interface ThreadsRepository {

    /**
     * Inserts the order in which local threads should be displayed.
     */
    public suspend fun insertThreadOrder(id: String, order: List<String>)

    /**
     * Retrieves the order in which local threads should be displayed.
     */
    public suspend fun selectThreadOrder(id: String): List<String>

    /**
     * Inserts the given list of [Thread].
     */
    public suspend fun insertThreads(threads: List<Thread>)

    /**
     * Upsert a [Message] in the corresponding thread (if such exists).
     */
    public suspend fun upsertMessageInThread(message: Message)

    /**
     * Upsert a list of [Message]s in the corresponding thread (if such exists).
     */
    public suspend fun upsertMessagesInThread(messages: List<Message>)

    /**
     * Retrieves the [Thread] identified by [id].
     *
     * @param id The ID of the [Thread] to retrieve.
     */
    public suspend fun selectThread(id: String): Thread?

    /**
     * Retrieves all [Thread]s identified by the supplied [ids].
     *
     * @param ids The identifiers of the [Thread]s to select.
     */
    public suspend fun selectThreads(ids: List<String>): List<Thread>

    /**
     * Deletes all threads from a channel.
     *
     * @param cid The ID of the channel to delete the threads from.
     */
    public suspend fun deleteChannelThreads(cid: String)

    /**
     * Deletes all data related to threads.
     */
    public suspend fun clear()
}
