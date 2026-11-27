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

import io.getstream.chat.android.client.internal.offline.repository.database.internal.ChatStore
import io.getstream.chat.android.client.internal.offline.repository.database.internal.WriteRecorder
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomThread
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric provides a working android.util.LruCache, which backs the thread cache.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class DatabaseThreadsRepositoryTest {

    private val recorder = WriteRecorder()
    private val writes = mutableListOf<Int?>()
    private val threadDao: ThreadDao = mock()
    private val threadOrderDao: ThreadOrderDao = mock()
    private val thread = randomThread(latestReplies = emptyList(), draftMessage = null)
    private val reply = randomMessage(parentId = thread.parentMessageId)

    @Test
    fun `insertThreads stores the threads in one write`() = runTest {
        whenever(threadDao.insertThreads(any())) doSuspendableAnswer { record() }

        repository().insertThreads(listOf(thread))

        writes shouldBeEqualTo listOf(1)
    }

    @Test
    fun `insertThreadOrder stores the order in one write`() = runTest {
        whenever(threadOrderDao.insertThreadOrder(any())) doSuspendableAnswer { record() }

        repository().insertThreadOrder("query", listOf(thread.parentMessageId))

        writes shouldBeEqualTo listOf(1)
    }

    @Test
    fun `upsertMessageInThread reads and stores the thread in one write`() = runTest {
        givenStoredThread()

        repository().upsertMessageInThread(reply)

        writes shouldBeEqualTo listOf(1, 1)
        insertedThreads().single().latestReplyIds shouldBeEqualTo listOf(reply.id)
    }

    @Test
    fun `upsertMessagesInThread reads and stores the threads in one write`() = runTest {
        givenStoredThread()
        val second = randomMessage(parentId = thread.parentMessageId)

        repository().upsertMessagesInThread(listOf(reply, second, randomMessage(parentId = null)))

        writes shouldBeEqualTo listOf(1, 1)
        insertedThreads().single().latestReplyIds.toSet() shouldBeEqualTo setOf(reply.id, second.id)
    }

    @Test
    fun `upsertMessageInThread stores nothing for a thread that is not stored`() = runTest {
        whenever(threadDao.selectThreads(any())) doReturn emptyList()

        repository().upsertMessageInThread(reply)

        verify(threadDao, never()).insertThreads(any())
    }

    @Test
    fun `deleteChannelThreads and clear each delete in one write`() = runTest {
        whenever(threadDao.deleteThreads(any())) doSuspendableAnswer { record() }
        whenever(threadDao.deleteAll()) doSuspendableAnswer { record() }
        whenever(threadOrderDao.deleteAll()) doSuspendableAnswer { record() }
        val repository = repository()

        repository.deleteChannelThreads(thread.cid)
        repository.clear()

        writes shouldBeEqualTo listOf(1, 2, 2)
    }

    @Test
    fun `a thread of a write that is rolled back is not read from the cache`() = runTest {
        val failingStore = ChatStore { block ->
            block()
            error("rolled back")
        }
        val repository = repository(failingStore)

        val result = runCatching { repository.insertThreads(listOf(thread)) }

        result.isFailure shouldBeEqualTo true
        repository.selectThread(thread.parentMessageId) shouldBeEqualTo null
        verify(threadDao).selectThread(thread.parentMessageId)
    }

    @Test
    fun `selectThreads reads each thread from the database once and keeps the order of the ids`() = runTest {
        val other = randomThread(latestReplies = emptyList(), draftMessage = null)
        whenever(threadDao.selectThread(thread.parentMessageId)) doReturn thread.toEntity()
        whenever(threadDao.selectThread(other.parentMessageId)) doReturn other.toEntity()
        val ids = listOf(other.parentMessageId, thread.parentMessageId, "unknown")
        val repository = repository()

        val first = repository.selectThreads(ids)
        val second = repository.selectThreads(ids)

        first.map { it.parentMessageId } shouldBeEqualTo listOf(other.parentMessageId, thread.parentMessageId)
        second shouldBeEqualTo first
        verify(threadDao).selectThread(thread.parentMessageId)
        verify(threadDao).selectThread(other.parentMessageId)
    }

    private fun repository(store: ChatStore = recorder.store) = DatabaseThreadsRepository(
        store = store,
        threadDao = threadDao,
        threadOrderDao = threadOrderDao,
        getUser = { randomUser(id = it) },
        getMessage = { null },
        getChannel = { null },
        getDraftMessage = { null },
    )

    private suspend fun givenStoredThread() {
        whenever(threadDao.selectThreads(any())) doSuspendableAnswer {
            record()
            listOf(thread.toEntity())
        }
        whenever(threadDao.insertThreads(any())) doSuspendableAnswer { record() }
    }

    private suspend fun insertedThreads(): List<ThreadEntity> =
        argumentCaptor<List<ThreadEntity>>().apply { verify(threadDao).insertThreads(capture()) }.firstValue

    private fun record() {
        writes += recorder.writes.takeIf { recorder.isWriting }
    }
}
