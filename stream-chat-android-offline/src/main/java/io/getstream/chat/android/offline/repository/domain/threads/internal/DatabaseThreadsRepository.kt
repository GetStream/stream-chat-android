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

package io.getstream.chat.android.offline.repository.domain.threads.internal

import android.util.LruCache
import io.getstream.chat.android.client.extensions.internal.upsertReply
import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User

/**
 * Implementation of the [ThreadsRepository] backed by a database.
 *
 * @param threadDao The [ThreadDao] implementation for accessing the threads data.
 * @param threadOrderDao The [ThreadOrderDao] implementation for accessing the thread order data.
 * @param getUser Logic for retrieving a [User] by its ID.
 * @param getMessage Logic for retrieving a [Message] by its ID.
 * @param getChannel Logic for retrieving a [Channel] by its ID.
 * @param getDraftMessage Logic for retrieving a [DraftMessage] by the Message ID it belongs to.
 * @param cacheSize The number of threads to cache.
 */
internal class DatabaseThreadsRepository(
    private val threadDao: ThreadDao,
    private val threadOrderDao: ThreadOrderDao,
    private val getUser: suspend (userId: String) -> User,
    private val getMessage: suspend (messageId: String) -> Message?,
    private val getChannel: suspend (cid: String) -> Channel?,
    private val getDraftMessage: suspend (messageId: String) -> DraftMessage?,
    cacheSize: Int = 1000,
) : ThreadsRepository {

    private val cache = LruCache<String, Thread>(cacheSize)

    override suspend fun insertThreadOrder(id: String, order: List<String>) {
        threadOrderDao.insertThreadOrder(ThreadOrderEntity(id, order))
    }

    override suspend fun selectThreadOrder(id: String): List<String> {
        return threadOrderDao.selectThreadOrder(id)?.order.orEmpty()
    }

    override suspend fun insertThreads(threads: List<Thread>) {
        // Update cache
        threads.forEach { thread ->
            cache.put(thread.parentMessageId, thread)
        }
        // Update DB
        val entities = threads.map(Thread::toEntity)
        threadDao.insertThreads(entities)
    }

    override suspend fun upsertMessageInThread(message: Message) {
        val threadId = message.parentId ?: return
        // Check cache first, then read from DB
        val thread = cache[threadId]
            ?: threadDao.selectThread(threadId)?.toModel(getUser, getMessage, getChannel, getDraftMessage)
            ?: return
        val updatedThread = thread.upsertReply(message)
        // Update cache
        cache.put(threadId, updatedThread)
        // Update DB
        threadDao.insertThread(updatedThread.toEntity())
    }

    override suspend fun upsertMessagesInThread(messages: List<Message>) {
        val threadIds = messages.mapNotNull(Message::parentId).toSet()
        if (threadIds.isEmpty()) return
        // Check cache first, then read from DB
        val cachedThreads = threadIds.mapNotNull { id -> cache[id] }
        val cachedThreadIds = cachedThreads.map(Thread::parentMessageId).toSet()
        val dbThreadIds = threadIds - cachedThreadIds
        val databaseThreads = if (dbThreadIds.isEmpty()) {
            emptyList() // Prevent access to DB if all threads are cached
        } else {
            threadDao.selectThreads(dbThreadIds).map { thread ->
                thread.toModel(getUser, getMessage, getChannel, getDraftMessage)
            }
        }
        // Group messages by their thread (parentId)
        val replies = messages
            .filter { message -> message.parentId != null } // discard not relevant messages
            .groupBy { message -> message.parentId } // group by their threadId
        // Upsert each message in its thread
        val updatedThreads = (cachedThreads + databaseThreads)
            .map { thread ->
                val threadReplies = replies[thread.parentMessageId]
                var updatedThread = thread
                threadReplies?.forEach { reply ->
                    updatedThread = updatedThread.upsertReply(reply)
                }
                updatedThread
            }
            .onEach { updatedThread ->
                // Update cache
                cache.put(updatedThread.parentMessageId, updatedThread)
            }
            .map(Thread::toEntity)
        // Update DB
        threadDao.insertThreads(updatedThreads)
    }

    override suspend fun selectThread(id: String): Thread? {
        // Check cache first, then read from DB
        val thread = cache[id]
            ?: threadDao.selectThread(id)?.toModel(getUser, getMessage, getChannel, getDraftMessage)
            ?: return null
        // Update cache
        cache.put(id, thread)
        return thread
    }

    override suspend fun selectThreads(ids: List<String>): List<Thread> {
        // Important: Fetch threads sequentially so that their order is preserved (as the [ids] are ordered).
        val threads = ids.mapNotNull { id ->
            // Check cache first, then read from DB
            cache[id] ?: threadDao.selectThread(id)?.toModel(getUser, getMessage, getChannel, getDraftMessage)
        }.onEach { thread ->
            // Update cache
            cache.put(thread.parentMessageId, thread)
        }
        return threads
    }

    override suspend fun deleteChannelThreads(cid: String) {
        cache.evictAll()
        threadDao.deleteThreads(cid)
    }

    override suspend fun clear() {
        cache.evictAll()
        threadDao.deleteAll()
        threadOrderDao.deleteAll()
    }
}
