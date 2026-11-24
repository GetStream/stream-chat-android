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

import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.randomThread
import io.getstream.chat.android.randomThreadInfo
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ThreadResponseHandlerTest {

    private val firstList: QueryThreadsLogic = mock()
    private val secondList: QueryThreadsLogic = mock()
    private val logic: LogicRegistry = mock {
        on(it.getActiveQueryThreadsLogic()) doReturn listOf(firstList, secondList)
    }
    private val repos: RepositoryFacade = mock()
    private val handler = ThreadResponseHandler(logic, repos)

    @Test
    fun `onThreadFetched stores the thread and replaces it in every loaded list`() = runTest {
        val thread = randomThread(parentMessageId = THREAD_ID, updatedAt = Date(2_000))
        whenever(repos.selectThread(THREAD_ID)) doReturn randomThread(parentMessageId = THREAD_ID, updatedAt = Date(1_000))

        handler.onThreadFetched(thread)

        verify(repos).insertThreads(listOf(thread))
        verify(firstList).replaceThread(thread)
        verify(secondList).replaceThread(thread)
    }

    @Test
    fun `onThreadFetched stores a thread that is not stored yet`() = runTest {
        val thread = randomThread(parentMessageId = THREAD_ID)

        handler.onThreadFetched(thread)

        verify(repos).insertThreads(listOf(thread))
    }

    @Test
    fun `onThreadFetched keeps a stored thread newer than the response`() = runTest {
        val thread = randomThread(parentMessageId = THREAD_ID, updatedAt = Date(1_000))
        whenever(repos.selectThread(THREAD_ID)) doReturn randomThread(parentMessageId = THREAD_ID, updatedAt = Date(2_000))

        handler.onThreadFetched(thread)

        verify(repos, never()).insertThreads(any())
        verify(firstList).replaceThread(thread)
    }

    @Test
    fun `onThreadPartiallyUpdated applies the response to the stored thread and the loaded lists`() = runTest {
        val stored = randomThread(parentMessageId = THREAD_ID, title = "old", updatedAt = Date(1_000))
        whenever(repos.selectThread(THREAD_ID)) doReturn stored
        val threadInfo = randomThreadInfo(parentMessageId = THREAD_ID, title = "new", updatedAt = Date(2_000))
            .copy(extraData = mapOf("color" to "red"))

        handler.onThreadPartiallyUpdated(threadInfo)

        val saved = argumentCaptor<List<Thread>>().apply { verify(repos).insertThreads(capture()) }.firstValue.single()
        saved shouldBeEqualTo stored.copy(title = "new", updatedAt = Date(2_000), extraData = mapOf("color" to "red"))
        verify(firstList).applyThreadInfo(threadInfo)
        verify(secondList).applyThreadInfo(threadInfo)
    }

    @Test
    fun `onThreadPartiallyUpdated does not store a thread that is not stored yet`() = runTest {
        val threadInfo = randomThreadInfo(parentMessageId = THREAD_ID)

        handler.onThreadPartiallyUpdated(threadInfo)

        verify(repos, never()).insertThreads(any())
        verify(firstList).applyThreadInfo(threadInfo)
    }

    @Test
    fun `onThreadPartiallyUpdated keeps a stored thread newer than the response`() = runTest {
        whenever(repos.selectThread(THREAD_ID)) doReturn randomThread(parentMessageId = THREAD_ID, updatedAt = Date(2_000))

        handler.onThreadPartiallyUpdated(randomThreadInfo(parentMessageId = THREAD_ID, updatedAt = Date(1_000)))

        verify(repos, never()).insertThreads(any())
    }

    @Test
    fun `a storage failure is logged and the loaded lists are still updated`() = runTest {
        whenever(repos.selectThread(THREAD_ID)) doThrow IllegalStateException("db closed")
        val thread = randomThread(parentMessageId = THREAD_ID)
        val threadInfo = randomThreadInfo(parentMessageId = THREAD_ID)

        handler.onThreadFetched(thread)
        handler.onThreadPartiallyUpdated(threadInfo)

        verify(firstList).replaceThread(thread)
        verify(firstList).applyThreadInfo(threadInfo)
    }

    @Test
    fun `a list update failure is logged and the thread is still stored`() = runTest {
        whenever(firstList.replaceThread(any())) doThrow IllegalStateException("list failed")
        val thread = randomThread(parentMessageId = THREAD_ID)

        handler.onThreadFetched(thread)

        verify(repos).insertThreads(listOf(thread))
    }

    private companion object {
        const val THREAD_ID = "parent-message-id"
    }
}
