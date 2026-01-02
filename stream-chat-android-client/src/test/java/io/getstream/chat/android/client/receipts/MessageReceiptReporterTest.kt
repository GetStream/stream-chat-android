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

package io.getstream.chat.android.client.receipts

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.persistence.repository.MessageReceiptRepository
import io.getstream.chat.android.client.randomMessageReceipt
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
import org.mockito.kotlin.wheneverBlocking
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
            .givenMarkDelivered(messages)
        val sut = fixture.get(backgroundScope)

        sut.start()
        advanceTimeBy(100) // Allow initial execution

        fixture.verifyMarkDeliveredCalled(messages = messages)
        val messageIds = messages.map(Message::id)
        fixture.verifyDeleteByMessageIdsCalled(messageIds = messageIds)
    }

    @Test
    fun `should not delete receipts when marking messages as delivered fails`() = runTest {
        val fixture = Fixture()
            .givenMessageReceipts(listOf(randomMessageReceipt()))
            // Simulate an error when marking messages as delivered
            .givenMarkDelivered(error = mock())
        val sut = fixture.get(backgroundScope)

        sut.start()
        advanceTimeBy(100) // Allow initial execution

        fixture.verifyDeleteByMessageIdsCalled(never())

        // Keep processing in the next time window
        fixture.givenMarkDelivered()
        advanceTimeBy(1000)

        fixture.verifyMarkDeliveredCalled(times(2))
        fixture.verifyDeleteByMessageIdsCalled()
    }

    @Test
    fun `should handle empty receipt list`() = runTest {
        val fixture = Fixture()
            .givenMessageReceipts(receipts = emptyList())
        val sut = fixture.get(backgroundScope)

        sut.start()
        advanceTimeBy(100) // Allow initial execution

        fixture.verifyMarkDeliveredCalled(never())
        fixture.verifyDeleteByMessageIdsCalled(never())
    }

    @Test
    fun `should execute periodically with correct delay`() = runTest {
        val fixture = Fixture()
            .givenMessageReceipts(listOf(randomMessageReceipt()))
            .givenMarkDelivered()
        val sut = fixture.get(backgroundScope)

        sut.start()
        advanceTimeBy(100) // Allow initial execution

        advanceTimeBy(1000) // Advance to the second interval

        advanceTimeBy(1000) // Advance to the third interval

        advanceTimeBy(1000) // Advance to the fourth interval

        fixture.verifyMarkDeliveredCalled(times(4))
    }

    @Test
    fun `should stop execution when coroutine scope is cancelled`() = runTest {
        val fixture = Fixture()
            .givenMessageReceipts(listOf(randomMessageReceipt()))
            .givenMarkDelivered()
        val sut = fixture.get(backgroundScope)

        sut.start()
        advanceTimeBy(100) // Allow initial execution

        backgroundScope.cancel()

        advanceTimeBy(1000) // Try to advance time after cancellation

        fixture.verifyMarkDeliveredCalled(times(1))
    }

    private class Fixture {
        private val mockMessageReceiptRepository = mock<MessageReceiptRepository>()
        private val mockApi = mock<ChatApi>()

        fun givenMessageReceipts(receipts: List<MessageReceipt>) = apply {
            wheneverBlocking { mockMessageReceiptRepository.selectMessageReceipts(limit = 100) } doReturn receipts
        }

        fun givenMarkDelivered(messages: List<Message>? = null, error: Error? = null) = apply {
            whenever(mockApi.markDelivered(messages ?: any())) doReturn
                (error?.asCall() ?: Unit.asCall())
        }

        fun verifyMarkDeliveredCalled(
            mode: VerificationMode = times(1),
            messages: List<Message>? = null,
        ) {
            verify(mockApi, mode).markDelivered(messages ?: any())
        }

        fun verifyDeleteByMessageIdsCalled(
            mode: VerificationMode = times(1),
            messageIds: List<String>? = null,
        ) {
            verifyBlocking(mockMessageReceiptRepository, mode) {
                deleteMessageReceiptsByMessageIds(messageIds ?: any())
            }
        }

        fun get(scope: CoroutineScope) = MessageReceiptReporter(
            scope = scope,
            messageReceiptRepository = mockMessageReceiptRepository,
            api = mockApi,
        )
    }
}
