/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.receipts

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistance.repository.MessageReceiptRepository
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReceipt
import io.getstream.chat.android.randomMessageReceipt
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.mockito.verification.VerificationMode

@OptIn(ExperimentalCoroutinesApi::class)
internal class MessageReceiptReporterTest {

    @Test
    fun `should fetch and send delivery receipts successfully`() = runTest {
        val receipts = listOf(
            randomMessageReceipt(),
            randomMessageReceipt(),
        )
        val messages = receipts.map { receipt ->
            Message(
                id = receipt.messageId,
                cid = receipt.cid,
            )
        }
        val fixture = Fixture()
            .givenMessageReceipts(receipts)
            .givenMarkMessagesAsDelivered(messages)
        val sut = fixture.get(backgroundScope)

        sut.init()
        advanceTimeBy(1100) // Advance time to after the interval window

        fixture.verifyMarkMessagesAsDeliveredCalled(messages = messages)
        val messageIds = messages.map(Message::id)
        fixture.verifyDeleteByMessageIdsCalled(messageIds = messageIds)
    }

    @Test
    fun `should not delete receipts when marking messages as delivered fails`() = runTest {
        val fixture = Fixture()
            .givenMessageReceipts(listOf(randomMessageReceipt()))
            .givenMarkMessagesAsDelivered(error = mock())
        val sut = fixture.get(backgroundScope)

        sut.init()
        advanceTimeBy(1100) // Allow initial execution

        fixture.verifyDeleteByMessageIdsCalled(never())

        // Keep processing subsequent success emissions
        fixture.givenMessageReceipts(listOf(randomMessageReceipt()))
        fixture.givenMarkMessagesAsDelivered()

        advanceTimeBy(1100)

        fixture.verifyMarkMessagesAsDeliveredCalled(times(2))
        fixture.verifyDeleteByMessageIdsCalled()
    }

    @Test
    fun `should handle empty receipt list`() = runTest {
        val fixture = Fixture()
            .givenMessageReceipts(receipts = emptyList())
        val sut = fixture.get(backgroundScope)

        sut.init()
        advanceTimeBy(1100)

        fixture.verifyMarkMessagesAsDeliveredCalled(never())
        fixture.verifyDeleteByMessageIdsCalled(never())
    }

    @Test
    fun `should execute periodically with correct delay`() = runTest {
        val fixture = Fixture()
            .givenMessageReceipts(listOf(randomMessageReceipt()))
            .givenMarkMessagesAsDelivered()
        val sut = fixture.get(backgroundScope)

        sut.init()

        // Trigger multiple emissions

        advanceTimeBy(1100) // Collecting the first emission

        // Trigger a new list
        fixture.givenMessageReceipts(listOf(randomMessageReceipt()))
        advanceTimeBy(1000) // Wait for delay

        // Trigger a new list
        fixture.givenMessageReceipts(listOf(randomMessageReceipt()))
        advanceTimeBy(100) // Collecting the second emission

        // Trigger a new list
        fixture.givenMessageReceipts(listOf(randomMessageReceipt()))
        advanceTimeBy(1000) // Wait for delay
        advanceTimeBy(100) // Collecting the third emission

        fixture.verifyMarkMessagesAsDeliveredCalled(times(3))
    }

    @Test
    fun `should stop execution when coroutine scope is cancelled`() = runTest {
        val fixture = Fixture()
            .givenMessageReceipts(listOf(randomMessageReceipt()))
            .givenMarkMessagesAsDelivered()
        val sut = fixture.get(backgroundScope)

        sut.init()
        advanceTimeBy(1100) // Allow initial execution

        backgroundScope.cancel()

        // Trigger a new list
        fixture.givenMessageReceipts(listOf(randomMessageReceipt()))

        advanceTimeBy(2000) // Try to advance time after cancellation

        fixture.verifyMarkMessagesAsDeliveredCalled(times(1))
    }

    private class Fixture {
        private val mockChatClient = mock<ChatClient>()

        private val receiptsStateFlow = MutableStateFlow<List<MessageReceipt>>(emptyList())

        private val mockMessageReceiptRepository = mock<MessageReceiptRepository> {
            onBlocking { getAllByType(type = MessageReceipt.TYPE_DELIVERY, limit = 100) } doReturn receiptsStateFlow
        }

        fun givenMessageReceipts(receipts: List<MessageReceipt>) = apply {
            receiptsStateFlow.value = receipts
        }

        fun givenMarkMessagesAsDelivered(messages: List<Message>? = null, error: Error? = null) = apply {
            whenever(mockChatClient.markMessagesAsDelivered(messages ?: any())) doReturn
                (error?.asCall() ?: Unit.asCall())
        }

        fun verifyMarkMessagesAsDeliveredCalled(mode: VerificationMode = times(1), messages: List<Message>? = null) {
            verify(mockChatClient, mode).markMessagesAsDelivered(messages ?: any())
        }

        fun verifyDeleteByMessageIdsCalled(mode: VerificationMode = times(1), messageIds: List<String>? = null) {
            verifyBlocking(mockMessageReceiptRepository, mode) { deleteByMessageIds(messageIds ?: any()) }
        }

        fun get(scope: CoroutineScope) = MessageReceiptReporter(
            scope = scope,
            chatClient = mockChatClient,
            messageReceiptRepository = mockMessageReceiptRepository,
        )
    }
}
