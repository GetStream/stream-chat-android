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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal.ThreadStateLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ThreadQueryListenerStateTest {

    private val message = randomMessage()
    private val messageList = listOf(message, randomMessage(), randomMessage())

    private val threadLogic: ThreadLogic = mock()

    private val logic: LogicRegistry = mock {
        on(it.thread(message.id)) doReturn threadLogic
    }

    private val threadQueryListenerState = ThreadQueryListenerState(logic)

    @Test
    fun `given a request is already running, new requests are not allowed`() = runTest {
        whenever(threadLogic.isLoadingMessages()) doReturn true

        val result = threadQueryListenerState.onGetRepliesPrecondition(message.id)

        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `given a request is not running, new requests are allowed`() = runTest {
        whenever(threadLogic.isLoadingMessages()) doReturn false

        val result = threadQueryListenerState.onGetRepliesPrecondition(message.id)

        result shouldBeInstanceOf Result.Success::class
    }

    @Test
    fun `given a request is already running for more replies, new requests are not allowed`() = runTest {
        whenever(threadLogic.isLoadingMessages()) doReturn true

        val result = threadQueryListenerState.onGetRepliesPrecondition(message.id)

        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `given a request is not running for more replies, new requests are allowed`() = runTest {
        whenever(threadLogic.isLoadingMessages()) doReturn false

        val result = threadQueryListenerState.onGetRepliesPrecondition(message.id)

        result shouldBeInstanceOf Result.Success::class
    }

    @Test
    fun `given a request for replies is made, the SDK should be notified that it is running`() = runTest {
        threadQueryListenerState.onGetRepliesRequest(message.id, randomInt())

        verify(threadLogic).setLoading(true)
    }

    @Test
    fun `given a request for replies is made for more messages, the SDK should be notified that it is running`() =
        runTest {
            threadQueryListenerState.onGetRepliesMoreRequest(message.id, randomString(), randomInt())

            verify(threadLogic).setLoading(true)
        }

    @Test
    fun `given response it successful, the state should be updated in the SDK`() = runTest {
        threadQueryListenerState.onGetRepliesResult(Result.Success(messageList), message.id, 30)

        verify(threadLogic).setLoading(false)
        verify(threadLogic).setEndOfNewerMessages(true)
        verify(threadLogic).updateOldestMessageInThread(messageList)
        verify(threadLogic).setEndOfOlderMessages(true)
        verify(threadLogic).upsertMessages(messageList)
        verify(threadLogic, never()).setReplies(any(), any())
    }

    @Test
    fun `given a jump page, the newest page replaces it`() = runTest {
        whenever(threadLogic.isJumpPage) doReturn true

        threadQueryListenerState.onGetRepliesResult(Result.Success(messageList), message.id, 30)

        verify(threadLogic).setEndOfNewerMessages(true)
        verify(threadLogic).updateOldestMessageInThread(messageList)
        verify(threadLogic).updateNewestMessageInThread(messageList)
        verify(threadLogic).setEndOfOlderMessages(true)
        verify(threadLogic).setReplies(messageList, isJumpPage = false)
        verify(threadLogic, never()).upsertMessages(any())
    }

    @Test
    fun `given a jump page and a newest page as big as the limit, the older end is not reached`() = runTest {
        whenever(threadLogic.isJumpPage) doReturn true

        threadQueryListenerState.onGetRepliesResult(Result.Success(messageList), message.id, messageList.size)

        verify(threadLogic).setEndOfOlderMessages(false)
        verify(threadLogic).setReplies(messageList, isJumpPage = false)
    }

    @Test
    fun `given a reply held on a jump page, it is kept when the newest page replaces the jump page`() = runTest {
        val parentId = randomString()
        val state = ThreadMutableState(parentId, backgroundScope)
        val realThreadLogic = ThreadLogic(ThreadStateLogic(state), backgroundScope)
        val registry: LogicRegistry = mock { on(it.thread(parentId)) doReturn realThreadLogic }
        val listener = ThreadQueryListenerState(registry)
        val newestPage = listOf(reply(parentId, createdAt = 20))
        val held = reply(parentId, createdAt = 30)
        state.isJumpPage = true
        state.holdReply(held)

        listener.onGetRepliesResult(Result.Success(newestPage), parentId, 30)

        state.rawMessage.value.keys shouldBeEqualTo (newestPage.map { it.id } + held.id).toSet()
        state.heldReplies shouldBeEqualTo emptyMap()
    }

    @Test
    fun `given the replies were replaced while an older page was loading, the older page is dropped`() = runTest {
        val firstId = randomString()
        whenever(threadLogic.replaceCount) doReturn 0
        threadQueryListenerState.onGetRepliesMoreRequest(message.id, firstId, 30)
        whenever(threadLogic.replaceCount) doReturn 1

        threadQueryListenerState.onGetRepliesMoreResult(Result.Success(messageList), message.id, firstId, 30)

        verify(threadLogic).setLoading(false)
        verify(threadLogic, never()).upsertMessages(any())
    }

    @Test
    fun `given the replies were not replaced while an older page was loading, the older page is added`() = runTest {
        val firstId = randomString()
        threadQueryListenerState.onGetRepliesMoreRequest(message.id, firstId, 30)

        threadQueryListenerState.onGetRepliesMoreResult(Result.Success(messageList), message.id, firstId, 30)

        verify(threadLogic).upsertMessages(messageList)
    }

    @Test
    fun `given the replies were replaced while a newer page was loading, the newer page is dropped`() = runTest {
        val lastId = randomString()
        whenever(threadLogic.replaceCount) doReturn 0
        threadQueryListenerState.onGetNewerRepliesRequest(message.id, 30, lastId)
        whenever(threadLogic.replaceCount) doReturn 1

        threadQueryListenerState.onGetNewerRepliesResult(Result.Success(messageList), message.id, 30, lastId)

        verify(threadLogic).setLoading(false)
        verify(threadLogic, never()).upsertMessages(any())
        verify(threadLogic, never()).setEndOfNewerMessages(any())
    }

    @Test
    fun `given a first page from the oldest replies, the jump page is left`() = runTest {
        val stateLogic: ThreadStateLogic = mock()
        whenever(threadLogic.stateLogic()) doReturn stateLogic

        threadQueryListenerState.onGetNewerRepliesResult(Result.Success(messageList), message.id, 30, null)

        verify(stateLogic).leaveJumpPage()
        verify(threadLogic).upsertMessages(messageList)
    }

    @Test
    fun `given a newer page after a loaded reply, the jump page is kept`() = runTest {
        val stateLogic: ThreadStateLogic = mock()
        whenever(threadLogic.stateLogic()) doReturn stateLogic

        threadQueryListenerState.onGetNewerRepliesResult(Result.Success(messageList), message.id, 30, randomString())

        verify(stateLogic, never()).leaveJumpPage()
        verify(threadLogic).upsertMessages(messageList)
    }

    @Test
    fun `given response it failure, the state should NOT be updated in the SDK`() = runTest {
        threadQueryListenerState.onGetRepliesResult(Result.Failure(Error.GenericError("")), message.id, 30)

        verify(threadLogic).setLoading(false)

        verify(threadLogic, never()).setReplies(any(), any())
        verify(threadLogic, never()).setEndOfOlderMessages(any())
    }

    @Test
    fun `given a request for replies around a reply is made, loading starts and the newer end is reset`() =
        runTest {
            threadQueryListenerState.onGetRepliesAroundRequest(message.id, randomString(), randomInt())

            verify(threadLogic).setLoading(true)
            verify(threadLogic).setEndOfNewerMessages(false)
        }

    @Test
    fun `given the reply around which the page was loaded is in the middle, neither end is reached`() = runTest {
        val messages = List(5) { randomMessage() }

        threadQueryListenerState.onGetRepliesAroundResult(Result.Success(messages), message.id, messages[2].id, 5)

        verify(threadLogic).setLoading(false)
        verify(threadLogic).updateOldestMessageInThread(messages)
        verify(threadLogic).updateNewestMessageInThread(messages)
        verify(threadLogic).setEndOfOlderMessages(false)
        verify(threadLogic).setEndOfNewerMessages(false)
        verify(threadLogic).setReplies(messages, isJumpPage = true)
        verify(threadLogic, never()).upsertMessages(any())
        verify(threadLogic, never()).setEndOfOlderMessages(true)
        verify(threadLogic, never()).setEndOfNewerMessages(true)
    }

    @Test
    fun `given the reply around which the page was loaded is in the second half, the newer end is reached`() =
        runTest {
            val messages = List(5) { randomMessage() }

            threadQueryListenerState.onGetRepliesAroundResult(Result.Success(messages), message.id, messages[4].id, 5)

            verify(threadLogic).setEndOfNewerMessages(true)
            verify(threadLogic, never()).setEndOfOlderMessages(true)
        }

    @Test
    fun `given an even page around a reply, both central positions count as the middle`() = runTest {
        val messages = List(30) { randomMessage() }

        threadQueryListenerState.onGetRepliesAroundResult(Result.Success(messages), message.id, messages[14].id, 30)
        threadQueryListenerState.onGetRepliesAroundResult(Result.Success(messages), message.id, messages[15].id, 30)

        verify(threadLogic, never()).setEndOfOlderMessages(true)
        verify(threadLogic, never()).setEndOfNewerMessages(true)
    }

    @Test
    fun `given an even page around a reply, a target after the central positions reaches the newer end`() = runTest {
        val messages = List(30) { randomMessage() }

        threadQueryListenerState.onGetRepliesAroundResult(Result.Success(messages), message.id, messages[16].id, 30)

        verify(threadLogic).setEndOfNewerMessages(true)
        verify(threadLogic, never()).setEndOfOlderMessages(true)
    }

    @Test
    fun `given the reply around which the page was loaded is in the first half, the older end is reached`() = runTest {
        val messages = List(5) { randomMessage() }

        threadQueryListenerState.onGetRepliesAroundResult(Result.Success(messages), message.id, messages[0].id, 5)

        verify(threadLogic).setEndOfOlderMessages(true)
        verify(threadLogic, never()).setEndOfNewerMessages(true)
    }

    @Test
    fun `given an empty page around a reply, both ends are reached`() = runTest {
        threadQueryListenerState.onGetRepliesAroundResult(
            Result.Success(emptyList()),
            message.id,
            randomString(),
            30,
        )

        verify(threadLogic).setEndOfOlderMessages(true)
        verify(threadLogic).setEndOfNewerMessages(true)
        verify(threadLogic).setReplies(emptyList(), isJumpPage = true)
    }

    @Test
    fun `given the page around a reply is smaller than the limit, both ends are reached`() = runTest {
        threadQueryListenerState.onGetRepliesAroundResult(
            Result.Success(messageList),
            message.id,
            messageList[1].id,
            30,
        )

        verify(threadLogic).setLoading(false)
        verify(threadLogic).setEndOfOlderMessages(true)
        verify(threadLogic).setEndOfNewerMessages(true)
        verify(threadLogic).setReplies(messageList, isJumpPage = true)
    }

    @Test
    fun `given response for replies around a reply is failure, the state should NOT be updated in the SDK`() =
        runTest {
            threadQueryListenerState.onGetRepliesAroundResult(
                Result.Failure(Error.GenericError("")),
                message.id,
                randomString(),
                30,
            )

            verify(threadLogic).setLoading(false)

            verify(threadLogic, never()).setReplies(any(), any())
            verify(threadLogic, never()).setEndOfOlderMessages(any())
        }

    private fun reply(parentId: String, createdAt: Long) = randomMessage(
        parentId = parentId,
        createdAt = Date(createdAt),
        createdLocallyAt = null,
        syncStatus = SyncStatus.COMPLETED,
    )
}
