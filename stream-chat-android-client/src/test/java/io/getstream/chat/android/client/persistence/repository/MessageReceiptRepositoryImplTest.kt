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

package io.getstream.chat.android.client.persistence.repository

import io.getstream.chat.android.client.persistence.db.dao.MessageReceiptDao
import io.getstream.chat.android.client.persistence.db.entity.MessageReceiptEntity
import io.getstream.chat.android.client.randomMessageReceipt
import io.getstream.chat.android.client.randomMessageReceiptEntity
import io.getstream.chat.android.client.receipts.MessageReceipt
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.wheneverBlocking

internal class MessageReceiptRepositoryImplTest {

    @Test
    fun `upsert message receipts`() = runTest {
        val receipt = randomMessageReceipt()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.upsertMessageReceipts(receipts = listOf(receipt))

        val expectedReceipts = listOf(
            MessageReceiptEntity(
                messageId = receipt.messageId,
                cid = receipt.cid,
                createdAt = receipt.createdAt,
            ),
        )
        fixture.verifyUpsertCalled(expectedReceipts)
    }

    @Test
    fun `get message receipts by type`() = runTest {
        val limit = randomInt()
        val receipt = randomMessageReceiptEntity()
        val fixture = Fixture()
            .givenMessageReceiptsByType(
                limit = limit,
                receipts = listOf(receipt),
            )
        val sut = fixture.get()

        val actual = sut.selectMessageReceipts(limit)

        val expected = listOf(
            MessageReceipt(
                messageId = receipt.messageId,
                cid = receipt.cid,
                createdAt = receipt.createdAt,
            ),
        )
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `delete message receipts by message IDs`() = runTest {
        val messageIds = listOf(randomString())
        val fixture = Fixture()
        val sut = fixture.get()

        sut.deleteMessageReceiptsByMessageIds(messageIds)

        fixture.verifyDeleteByMessageIdsCalled(messageIds)
    }

    @Test
    fun `clear message receipts`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.clearMessageReceipts()

        fixture.verifyDeleteAllCalled()
    }

    private class Fixture {

        private val mockDao = mock<MessageReceiptDao> {
            onBlocking { upsert(any()) } doReturn Unit
            onBlocking { deleteByMessageIds(any()) } doReturn Unit
        }

        fun givenMessageReceiptsByType(limit: Int, receipts: List<MessageReceiptEntity>) = apply {
            wheneverBlocking { mockDao.selectAll(limit) } doReturn receipts
        }

        fun verifyUpsertCalled(receipts: List<MessageReceiptEntity>) {
            verifyBlocking(mockDao) { upsert(receipts) }
        }

        fun verifyDeleteByMessageIdsCalled(messageIds: List<String>) {
            verifyBlocking(mockDao) { deleteByMessageIds(messageIds) }
        }

        fun verifyDeleteAllCalled() {
            verifyBlocking(mockDao) { deleteAll() }
        }

        fun get() = MessageReceiptRepositoryImpl(dao = mockDao)
    }
}
