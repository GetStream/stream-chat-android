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

package io.getstream.chat.android.client.internal.offline.repository.domain.threads.internal

import android.util.LruCache
import io.getstream.chat.android.client.extensions.internal.upsertReply
import io.getstream.chat.android.client.internal.offline.repository.database.internal.ChatStore
import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.User

/**
 * Implementation of the [ThreadsRepository] backed by a database.
 *
 * @param store Runs the writes. A write that reads a thread first reads it inside the same write.
 * @param threadDao The [ThreadDao] implementation for accessing the threads data.
 * @param threadOrderDao The [ThreadOrderDao] implementation for accessing the thread order data.
 * @param getUser Logic for retrieving a [User] by its ID.
 * @param getMessage Logic for retrieving a [Message] by its ID.
 * @param getChannel Logic for retrieving a [Channel] by its ID.
 * @param getDraftMessage Logic for retrieving a [DraftMessage] by the Message ID it belongs to.
 * @param cacheSize The number of threads to cache.
 */
@Suppress("LongParameterList")
internal class DatabaseThreadsRepository(
    private val store: ChatStore,
    private val threadDao: ThreadDao,
    private val threadOrderDao: ThreadOrderDao,
    private val getUser: suspend (userId: String) -> User,
    private val getMessage: suspend (messageId: String) -> Message?,
    private val getChannel: suspend (cid: String) -> Channel?,
    private val getDraftMessage: suspend (messageId: String) -> DraftMessage?,
    cacheSize: Int = 1000,
) : ThreadsRepository {

    private val cache = LruCache<String, Thread>(cacheSize)

    init {
        // A cached thread that was never stored would be returned by the next read.
        store.onRollback { cache.evictAll() }
    }

    override suspend fun insertThreadOrder(id: String, order: List<String>) {
        store.write { threadOrderDao.insertThreadOrder(ThreadOrderEntity(id, order)) }
    }

    override suspend fun selectThreadOrder(id: String): List<String> {
        return threadOrderDao.selectThreadOrder(id)?.order.orEmpty()
    }

    override suspend fun insertThreads(threads: List<Thread>) = store.write {
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
        store.write { upsertReplies(setOf(threadId), listOf(message)) }
    }

    override suspend fun upsertMessagesInThread(messages: List<Message>) {
        val threadIds = messages.mapNotNull(Message::parentId).toSet()
        if (threadIds.isEmpty()) return
        store.write { upsertReplies(threadIds, messages) }
    }

    private suspend fun upsertReplies(threadIds: Set<String>, messages: List<Message>) {
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
        if (updatedThreads.isNotEmpty()) threadDao.insertThreads(updatedThreads)
    }

    // A thread read from the cache is not put back: a rollback may have evicted it in the meantime.
    override suspend fun selectThread(id: String): Thread? =
        cache[id]
            ?: threadDao.selectThread(id)?.toModel(getUser, getMessage, getChannel, getDraftMessage)
                ?.also { cache.put(id, it) }

    // Fetched one by one, so the threads keep the order of the [ids].
    override suspend fun selectThreads(ids: List<String>): List<Thread> = ids.mapNotNull { selectThread(it) }

    override suspend fun deleteChannelThreads(cid: String) = store.write {
        cache.evictAll()
        threadDao.deleteThreads(cid)
    }

    override suspend fun clear() = store.write {
        cache.evictAll()
        threadDao.deleteAll()
        threadOrderDao.deleteAll()
    }
}
