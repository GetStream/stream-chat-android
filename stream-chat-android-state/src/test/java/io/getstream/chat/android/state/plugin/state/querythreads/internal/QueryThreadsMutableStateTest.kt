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

package io.getstream.chat.android.state.plugin.state.querythreads.internal

import app.cash.turbine.test
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.Rule
import org.junit.jupiter.api.Test
import java.util.Date

internal class QueryThreadsMutableStateTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val threadList1 = listOf(
        Thread(
            activeParticipantCount = 2,
            cid = "messaging:123",
            channel = null,
            parentMessageId = "pmId1",
            parentMessage = Message(),
            createdByUserId = "usrId1",
            createdBy = null,
            replyCount = 1,
            participantCount = 2,
            threadParticipants = listOf(
                ThreadParticipant(null, "usrId1"),
                ThreadParticipant(null, "usrId1"),
            ),
            lastMessageAt = Date(),
            createdAt = Date(),
            updatedAt = Date(),
            deletedAt = null,
            title = "Thread 1",
            latestReplies = listOf(Message()),
            read = emptyList(),
        ),
    )

    private val threadList2 = listOf(
        Thread(
            activeParticipantCount = 2,
            cid = "messaging:124",
            channel = null,
            parentMessageId = "pmId2",
            parentMessage = Message(),
            createdByUserId = "usrId1",
            createdBy = null,
            replyCount = 1,
            participantCount = 2,
            threadParticipants = listOf(
                ThreadParticipant(null, "usrId1"),
                ThreadParticipant(null, "usrId1"),
            ),
            lastMessageAt = Date(),
            createdAt = Date(),
            updatedAt = Date(),
            deletedAt = null,
            title = "Thread 2",
            latestReplies = listOf(Message()),
            read = emptyList(),
        ),
    )

    @Test
    fun `Given QueryThreadsMutableState When calling setLoading Should update loading`() = runTest {
        // given
        val mutableState = QueryThreadsMutableState()
        mutableState.loading.test {
            val initialValue = awaitItem()
            initialValue `should be equal to` false
            // when
            mutableState.setLoading(true)
            val updatedValue = awaitItem()
            // then
            updatedValue `should be equal to` true

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given QueryThreadsMutableState When calling setLoadingMore Should update loadingMore`() = runTest {
        // given
        val mutableState = QueryThreadsMutableState()
        mutableState.loadingMore.test {
            val initialValue = awaitItem()
            initialValue `should be equal to` false
            // when
            mutableState.setLoadingMore(true)
            val updatedValue = awaitItem()
            // then
            updatedValue `should be equal to` true

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given QueryThreadsMutableState When calling setThreads Should update threads`() = runTest {
        // given
        val mutableState = QueryThreadsMutableState()
        mutableState.threads.test {
            val initialValue = awaitItem()
            initialValue `should be equal to` emptyList()

            // when
            mutableState.setThreads(threadList1)
            val updatedValue = awaitItem()
            updatedValue `should be equal to` threadList1

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given QueryThreadsMutableState When calling appendThreads Should update threads`() = runTest {
        // given
        val mutableState = QueryThreadsMutableState()
        mutableState.threads.test {
            val initialValue = awaitItem()
            initialValue `should be equal to` emptyList()

            // when
            mutableState.appendThreads(threadList1)
            val updatedValue1 = awaitItem()
            updatedValue1 `should be equal to` threadList1

            mutableState.appendThreads(threadList2)
            val updatedValue2 = awaitItem()
            updatedValue2 `should be equal to` (threadList1 + threadList2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given QueryThreadsMutableState When calling setNext Should update next`() = runTest {
        // given
        val mutableState = QueryThreadsMutableState()
        mutableState.next.test {
            val initialValue = awaitItem()
            initialValue `should be equal to` null
            // when
            mutableState.setNext("nextCursor")
            val updatedValue = awaitItem()
            // then
            updatedValue `should be equal to` "nextCursor"

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given QueryThreadsMutableState When calling addUnseenThreadId Should update unseenThreadIds`() = runTest {
        // given
        val mutableState = QueryThreadsMutableState()
        mutableState.unseenThreadIds.test {
            val initialValue = awaitItem()
            initialValue `should be equal to` emptySet()
            // when
            mutableState.addUnseenThreadId("threadId1")
            mutableState.addUnseenThreadId("threadId2")
            val updatedValue1 = awaitItem()
            val updatedValue2 = awaitItem()
            // then
            updatedValue1 `should be equal to` setOf("threadId1")
            updatedValue2 `should be equal to` setOf("threadId1", "threadId2")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given QueryThreadsMutableState When calling clearUnseenThreadIds Should update unseenThreadIds`() = runTest {
        // given
        val mutableState = QueryThreadsMutableState()
        mutableState.unseenThreadIds.test {
            val initialValue = awaitItem()
            initialValue `should be equal to` emptySet()
            // when
            mutableState.addUnseenThreadId("threadId1")
            mutableState.clearUnseenThreadIds()
            val updatedValue = awaitItem()
            val clearedSet = awaitItem()
            // then
            updatedValue `should be equal to` setOf("threadId1")
            clearedSet `should be equal to` emptySet()

            cancelAndIgnoreRemainingEvents()
        }
    }
}
