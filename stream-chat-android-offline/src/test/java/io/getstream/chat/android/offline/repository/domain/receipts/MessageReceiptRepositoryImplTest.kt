package io.getstream.chat.android.offline.repository.domain.receipts

import app.cash.turbine.test
import io.getstream.chat.android.models.MessageReceipt
import io.getstream.chat.android.offline.randomMessageReceiptEntity
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessageReceipt
import io.getstream.chat.android.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.wheneverBlocking

internal class MessageReceiptRepositoryImplTest {

    @Test
    fun `upsert receipts`() = runTest {
        val receipt = randomMessageReceipt()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.upsert(receipts = listOf(receipt))

        val expectedReceipts = listOf(
            MessageReceiptEntity(
                messageId = receipt.messageId,
                type = receipt.type,
                createdAt = receipt.createdAt,
                cid = receipt.cid,
            )
        )
        fixture.verifyUpsertCalled(expectedReceipts)
    }

    @Test
    fun `get receipts by type`() = runTest {
        val type = randomString()
        val limit = randomInt()
        val receipt = randomMessageReceiptEntity()
        val fixture = Fixture()
            .givenReceiptsByType(
                type = type,
                limit = limit,
                receipts = listOf(receipt),
            )
        val sut = fixture.get()

        sut.getAllByType(type, limit).test {
            val actual = awaitItem()

            val expected = listOf(
                MessageReceipt(
                    messageId = receipt.messageId,
                    type = receipt.type,
                    createdAt = receipt.createdAt,
                    cid = receipt.cid,
                )
            )
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `delete receipts by message IDs`() = runTest {
        val messageIds = listOf(randomString())
        val fixture = Fixture()
        val sut = fixture.get()

        sut.deleteByMessageIds(messageIds)

        fixture.verifyDeleteByMessageIdsCalled(messageIds)
    }

    @Test
    fun `clear receipts`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.clear()

        fixture.verifyDeleteAllCalled()
    }

    private class Fixture {

        private val receiptsStateFlow = MutableStateFlow<List<MessageReceiptEntity>>(emptyList())

        private val mockDao = mock<MessageReceiptDao> {
            onBlocking { upsert(any()) } doReturn Unit
            onBlocking { deleteByMessageIds(any()) } doReturn Unit
        }

        fun givenReceiptsByType(type: String, limit: Int, receipts: List<MessageReceiptEntity>) = apply {
            wheneverBlocking { mockDao.selectAllByType(type, limit) } doReturn
                receiptsStateFlow.apply { value = receipts }
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
