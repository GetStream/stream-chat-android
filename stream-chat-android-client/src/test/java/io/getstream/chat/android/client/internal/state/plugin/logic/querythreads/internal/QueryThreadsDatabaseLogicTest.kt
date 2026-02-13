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
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomThread
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryThreadsDatabaseLogicTest {

    private val repository: ThreadsRepository = mock()
    private val logic = QueryThreadsDatabaseLogic(repository)

    @Test
    fun `getLocalThreadsOrder should return thread order from repository`() = runTest {
        // Given
        val filter = null
        val sort = QuerySortByField.descByName<Thread>("last_message_at")
        val expectedOrder = listOf("thread1", "thread2", "thread3")
        val expectedQueryId = "${filter.hashCode()}-${sort.toDto().hashCode()}"

        whenever(repository.selectThreadOrder(expectedQueryId)) doReturn expectedOrder

        // When
        val result = logic.getLocalThreadsOrder(filter, sort)

        // Then
        Assertions.assertEquals(expectedOrder, result)
        verify(repository).selectThreadOrder(expectedQueryId)
    }

    @Test
    fun `setLocalThreadsOrder should insert thread order to repository`() = runTest {
        // Given
        val filter = Filters.eq("channel_cid", "messaging:456")
        val sort = QuerySortByField.ascByName<Thread>("created_at")
        val order = listOf("thread3", "thread1", "thread2")
        val expectedQueryId = "${filter.hashCode()}-${sort.toDto().hashCode()}"

        // When
        logic.setLocalThreadsOrder(filter, sort, order)

        // Then
        verify(repository).insertThreadOrder(expectedQueryId, order)
    }

    @Test
    fun `getLocalThreads should return threads from repository in specified order`() = runTest {
        // Given
        val threadIds = listOf("thread1", "thread2", "thread3")
        val expectedThreads = listOf(
            randomThread(parentMessageId = "thread1"),
            randomThread(parentMessageId = "thread2"),
            randomThread(parentMessageId = "thread3"),
        )

        whenever(repository.selectThreads(threadIds)) doReturn expectedThreads

        // When
        val result = logic.getLocalThreads(threadIds)

        // Then
        Assertions.assertEquals(expectedThreads, result)
        verify(repository).selectThreads(threadIds)
    }

    @Test
    fun `getLocalThreads should return empty list when no thread ids provided`() = runTest {
        // Given
        val threadIds = emptyList<String>()
        val expectedThreads = emptyList<Thread>()

        whenever(repository.selectThreads(threadIds)) doReturn expectedThreads

        // When
        val result = logic.getLocalThreads(threadIds)

        // Then
        Assertions.assertEquals(expectedThreads, result)
        verify(repository).selectThreads(threadIds)
    }
}
