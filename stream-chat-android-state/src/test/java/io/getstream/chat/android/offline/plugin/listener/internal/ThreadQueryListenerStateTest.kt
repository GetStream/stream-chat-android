package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class ThreadQueryListenerStateTest {

    private val message = randomMessage()
    private val messageList = listOf(message, randomMessage(), randomMessage())

    private val threadLogic: ThreadLogic = mock()

    private val logic: LogicRegistry = mock {
        on(it.thread(message.id)) doReturn threadLogic
    }
    private val messageRepository: MessageRepository = mock {
        onBlocking { it.selectMessagesForThread(any(), any()) } doReturn messageList
    }

    private val threadQueryListenerState = ThreadQueryListenerState(logic, messageRepository)

    @Test
    fun `given a request is already running, new requests are not allowed`() = runTest {
        whenever(threadLogic.isLoadingMessages()) doReturn true

        val result = threadQueryListenerState.onGetRepliesPrecondition(message.id, randomInt())

        result.isError `should be` true
    }

    @Test
    fun `given a request is not running, new requests are allowed`() = runTest {
        whenever(threadLogic.isLoadingMessages()) doReturn false

        val result = threadQueryListenerState.onGetRepliesPrecondition(message.id, randomInt())

        result.isSuccess `should be` true
    }

    @Test
    fun `given a request is already running for more replies, new requests are not allowed`() = runTest {
        whenever(threadLogic.isLoadingOlderMessages()) doReturn true

        val result = threadQueryListenerState.onGetRepliesMorePrecondition(message.id, randomString(), randomInt())

        result.isError `should be` true
    }

    @Test
    fun `given a request is not running for more replies, new requests are allowed`() = runTest {
        whenever(threadLogic.isLoadingOlderMessages()) doReturn false

        val result = threadQueryListenerState.onGetRepliesMorePrecondition(message.id, randomString(), randomInt())

        result.isSuccess `should be` true
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

            verify(threadLogic).setLoadingOlderMessages(true)
        }

    @Test
    fun `given the database returns values messages are upserted in the SDK`() = runTest {
        whenever(messageRepository.selectMessagesForThread(eq(message.id), any())) doReturn messageList
        whenever(threadLogic.getMessage(message.id)) doReturn message

        threadQueryListenerState.onGetRepliesRequest(message.id, randomInt())

        verify(threadLogic).upsertMessages(messageList)
    }

    @Test
    fun `given response it successful, the state should be updated in the SDK`() = runTest {
        threadQueryListenerState.onGetRepliesResult(Result.success(messageList), message.id, 30)

        verify(threadLogic).run {
            setLoading(false)
            upsertMessages(messageList)
            setEndOfOlderMessages(false)
        }
    }

    @Test
    fun `given response it failure, the state should NOT be updated in the SDK`() = runTest {
        threadQueryListenerState.onGetRepliesResult(Result.error(ChatError()), message.id, 30)

        verify(threadLogic).setLoading(false)

        verify(threadLogic, never()).run {
            upsertMessages(messageList)
            setEndOfOlderMessages(false)
        }
    }
}
