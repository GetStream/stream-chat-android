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

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistence.repository.MessageReceiptRepository
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class MessageReceiptReporterTest {

    @Test
    fun `should fetch and send delivery receipts successfully`() = runTest {
        val first = receipt(1, cid = "messaging:a")
        val second = receipt(2, cid = "messaging:b")
        val fixture = Fixture(newUserScope()).persist(listOf(first, second))

        fixture.reporter.start()
        runCurrent()

        fixture.assertReportedIds(listOf(first.messageId, second.messageId))
        assertEquals(listOf(first.cid, second.cid), fixture.reportedBatches.single().map { it.cid })
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
        advanceReportInterval()
        assertEquals(2, fixture.repository.selectCount)
        assertStaysIdle(fixture)
    }

    @Test
    fun `should select once when startup finds an empty repository`() = runTest {
        val fixture = Fixture(newUserScope())

        fixture.reporter.start()
        runCurrent()

        assertEquals(listOf(BATCH_LIMIT), fixture.repository.selectLimits)
        assertTrue(fixture.reportedBatches.isEmpty())
        assertStaysIdle(fixture)
    }

    @Test
    fun `should drain persisted batches on the report interval and then stay idle`() = runTest {
        val queued = List(QUEUE_SIZE) { index -> receipt(index) }
        val fixture = Fixture(newUserScope()).persist(queued)
        val firstBatch = queued.take(BATCH_LIMIT).map(MessageReceipt::messageId)
        val secondBatch = queued.drop(BATCH_LIMIT).take(BATCH_LIMIT).map(MessageReceipt::messageId)
        val thirdBatch = queued.drop(BATCH_LIMIT * 2).map(MessageReceipt::messageId)

        fixture.reporter.start()
        runCurrent()
        fixture.assertReportedIds(firstBatch)

        repeat(NOTIFICATION_BURST) { fixture.reporter.onReceiptsEnqueued() }
        advanceTimeBy(REPORT_INTERVAL_MS - 1)
        runCurrent()
        fixture.assertReportedIds(firstBatch)

        advanceTimeBy(1)
        runCurrent()
        fixture.assertReportedIds(firstBatch, secondBatch)

        repeat(NOTIFICATION_BURST) { fixture.reporter.onReceiptsEnqueued() }
        advanceTimeBy(REPORT_INTERVAL_MS - 1)
        runCurrent()
        fixture.assertReportedIds(firstBatch, secondBatch)

        advanceTimeBy(1)
        runCurrent()
        fixture.assertReportedIds(firstBatch, secondBatch, thirdBatch)
        assertEquals(firstBatch + secondBatch + thirdBatch, fixture.reportedBatches.flatten().map { it.id })

        advanceReportInterval()
        assertEquals(4, fixture.repository.selectCount)
        assertTrue(fixture.repository.selectLimits.all { it == BATCH_LIMIT })
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
        assertStaysIdle(fixture)
    }

    @Test
    fun `should retry a failed delivery on the report interval without another enqueue`() = runTest {
        val queued = receipt(1)
        val fixture = Fixture(newUserScope())
            .failDeliveries(times = 1)
            .persist(listOf(queued))

        fixture.reporter.start()
        runCurrent()
        assertEquals(listOf(queued), fixture.repository.snapshot())
        fixture.assertReportedIds(listOf(queued.messageId))

        advanceTimeBy(REPORT_INTERVAL_MS - 1)
        runCurrent()
        fixture.assertReportedIds(listOf(queued.messageId))

        advanceTimeBy(1)
        runCurrent()
        fixture.assertReportedIds(listOf(queued.messageId), listOf(queued.messageId))
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
        advanceReportInterval()
        assertEquals(3, fixture.repository.selectCount)
        assertStaysIdle(fixture)
    }

    @Test
    fun `should stop execution when coroutine scope is cancelled`() = runTest {
        val fixture = Fixture(backgroundScope).persist(listOf(receipt(1)))

        fixture.reporter.start()
        runCurrent()
        backgroundScope.cancel()
        advanceTimeBy(IDLE_PERIOD_MS)
        runCurrent()

        fixture.assertReportedIds(listOf(receipt(1).messageId))
        assertEquals(1, fixture.repository.selectCount)
    }

    @Test
    fun `should not query or send after cancellation while idle`() = runTest {
        val scope = newUserScope()
        val fixture = Fixture(scope)

        fixture.reporter.start()
        runCurrent()
        assertEquals(1, fixture.repository.selectCount)

        scope.coroutineContext.cancelChildren()
        runCurrent()
        assertTrue(fixture.reportedBatches.isEmpty())
        assertStaysIdle(fixture)
    }

    @Test
    fun `should not query or send after cancellation while retrying`() = runTest {
        val scope = newUserScope()
        val queued = receipt(1)
        val fixture = Fixture(scope)
            .failDeliveries(times = 5)
            .persist(listOf(queued))

        fixture.reporter.start()
        runCurrent()
        assertEquals(listOf(queued), fixture.repository.snapshot())
        fixture.assertReportedIds(listOf(queued.messageId))

        scope.coroutineContext.cancelChildren()
        runCurrent()
        assertEquals(1, fixture.repository.selectCount)
        fixture.assertReportedIds(listOf(queued.messageId))
        assertStaysIdle(fixture)
    }

    @Test
    fun `should drain persisted receipts after child cancellation and accept later enqueues`() = runTest {
        val scope = newUserScope()
        val fixture = Fixture(scope).persist(listOf(receipt(1)))
        fixture.reporter.start()
        runCurrent()
        advanceReportInterval()
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())

        scope.coroutineContext.cancelChildren()
        runCurrent()
        val persistedWhileStopped = receipt(2)
        fixture.persist(listOf(persistedWhileStopped))
        fixture.reporter.start()
        runCurrent()

        fixture.assertReportedIds(listOf(receipt(1).messageId), listOf(persistedWhileStopped.messageId))
        advanceReportInterval()
        assertStaysIdle(fixture)

        val enqueuedAfterRestart = receipt(3)
        fixture.repository.upsertMessageReceipts(listOf(enqueuedAfterRestart))
        fixture.reporter.onReceiptsEnqueued()
        runCurrent()
        fixture.assertReportedIds(
            listOf(receipt(1).messageId),
            listOf(persistedWhileStopped.messageId),
            listOf(enqueuedAfterRestart.messageId),
        )
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
    }

    @Test
    fun `should ignore repeated start while a drain is active`() = runTest {
        val fixture = Fixture(newUserScope()).persist(listOf(receipt(1)))
        val gate = fixture.armDeliveryGate()

        fixture.reporter.start()
        fixture.reporter.start()
        runCurrent()

        assertTrue(gate.entered.isCompleted)
        assertEquals(1, fixture.repository.selectCount)
        assertEquals(1, fixture.reportedBatches.size)
        gate.succeed()
        runCurrent()
        advanceReportInterval()
        assertEquals(2, fixture.repository.selectCount)
        assertStaysIdle(fixture)
    }

    @Test
    fun `should ignore repeated start while waiting for receipts`() = runTest {
        val fixture = Fixture(newUserScope())

        fixture.reporter.start()
        runCurrent()
        fixture.reporter.start()
        runCurrent()

        assertEquals(1, fixture.repository.selectCount)
        assertStaysIdle(fixture)
    }

    @Test
    fun `should resume when a receipt is enqueued while idle`() = runTest {
        val fixture = Fixture(newUserScope())
        fixture.reporter.start()
        runCurrent()
        assertEquals(1, fixture.repository.selectCount)

        val queued = receipt(1)
        fixture.repository.upsertMessageReceipts(listOf(queued))
        fixture.reporter.onReceiptsEnqueued()
        runCurrent()

        fixture.assertReportedIds(listOf(queued.messageId))
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
        advanceReportInterval()
        assertStaysIdle(fixture)
    }

    @Test
    fun `should not strand a receipt enqueued during an empty selection`() = runTest {
        val fixture = Fixture(newUserScope())
        val gate = fixture.repository.armSelectGate(returnEmpty = true)
        val queued = receipt(1)

        fixture.reporter.start()
        runCurrent()
        assertTrue(gate.entered.isCompleted)

        fixture.repository.upsertMessageReceipts(listOf(queued))
        fixture.reporter.onReceiptsEnqueued()
        gate.release.complete(Unit)
        runCurrent()

        fixture.assertReportedIds(listOf(queued.messageId))
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
        advanceReportInterval()
        assertStaysIdle(fixture)
    }

    @Test
    fun `should deliver a receipt enqueued during an in-flight report after the cooldown`() = runTest {
        val first = receipt(1)
        val second = receipt(2)
        val fixture = Fixture(newUserScope()).persist(listOf(first))
        val gate = fixture.armDeliveryGate()

        fixture.reporter.start()
        runCurrent()
        assertTrue(gate.entered.isCompleted)

        fixture.repository.upsertMessageReceipts(listOf(second))
        repeat(NOTIFICATION_BURST) { fixture.reporter.onReceiptsEnqueued() }
        gate.succeed()
        runCurrent()
        fixture.assertReportedIds(listOf(first.messageId))

        advanceTimeBy(REPORT_INTERVAL_MS - 1)
        runCurrent()
        fixture.assertReportedIds(listOf(first.messageId))
        advanceTimeBy(1)
        runCurrent()
        fixture.assertReportedIds(listOf(first.messageId), listOf(second.messageId))
        advanceReportInterval()
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
        assertTrue(fixture.repository.selectCount <= 4)
        assertStaysIdle(fixture)
    }

    @Test
    fun `should deliver a receipt enqueued during cooldown without skipping the interval`() = runTest {
        val first = receipt(1)
        val second = receipt(2)
        val fixture = Fixture(newUserScope()).persist(listOf(first))

        fixture.reporter.start()
        runCurrent()
        fixture.assertReportedIds(listOf(first.messageId))

        fixture.repository.upsertMessageReceipts(listOf(second))
        repeat(NOTIFICATION_BURST) { fixture.reporter.onReceiptsEnqueued() }
        advanceTimeBy(REPORT_INTERVAL_MS - 1)
        runCurrent()
        fixture.assertReportedIds(listOf(first.messageId))

        advanceTimeBy(1)
        runCurrent()
        fixture.assertReportedIds(listOf(first.messageId), listOf(second.messageId))
        advanceReportInterval()
        assertStaysIdle(fixture)
    }

    @Test
    fun `should collapse a burst of enqueue signals into one extra selection`() = runTest {
        val fixture = Fixture(newUserScope())
        fixture.reporter.start()
        runCurrent()

        repeat(SIGNAL_BURST) { fixture.reporter.onReceiptsEnqueued() }
        runCurrent()

        assertEquals(2, fixture.repository.selectCount)
        assertTrue(fixture.reportedBatches.isEmpty())
        assertStaysIdle(fixture)
    }

    @Test
    fun `should report a receipt only after the manager persists it`() = runTest {
        val fixture = Fixture(newUserScope())
        val connected = fixture.connectManager()
        // Yield before the write so a notification sent before persistence is observed immediately.
        fixture.repository.yieldBeforePersist = true
        fixture.reporter.start()
        runCurrent()
        assertEquals(1, fixture.repository.selectCount)
        assertTrue(fixture.reportedBatches.isEmpty())

        val stored = connected.manager.markMessageAsDelivered(connected.message)
        runCurrent()

        assertTrue(stored)
        fixture.assertReportedIds(listOf(connected.message.id))
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
        advanceReportInterval()
        assertStaysIdle(fixture)
    }

    @Test
    fun `should stay idle when the manager does not persist a receipt`() = runTest {
        val fixture = Fixture(newUserScope())
        val connected = fixture.connectManager()
        fixture.reporter.start()
        runCurrent()
        val selects = fixture.repository.selectCount

        connected.manager.markChannelsAsDelivered(emptyList())
        val ineligible = connected.manager.markMessageAsDelivered(
            connected.message.copy(user = connected.currentUser),
        )
        fixture.repository.failNextUpsert = true
        val failure = runCatching { connected.manager.markMessageAsDelivered(connected.message) }
        runCurrent()

        assertFalse(ineligible)
        assertTrue(failure.exceptionOrNull() is IllegalStateException)
        assertEquals(selects, fixture.repository.selectCount)
        assertTrue(fixture.reportedBatches.isEmpty())
        assertEquals(emptyList<MessageReceipt>(), fixture.repository.snapshot())
        assertStaysIdle(fixture)
    }

    private fun TestScope.newUserScope(): CoroutineScope =
        CoroutineScope(coroutineContext + SupervisorJob(backgroundScope.coroutineContext.job))

    private fun TestScope.advanceReportInterval() {
        advanceTimeBy(REPORT_INTERVAL_MS)
        runCurrent()
    }

    private fun TestScope.assertStaysIdle(fixture: Fixture) {
        val selects = fixture.repository.selectCount
        val reports = fixture.reportedBatches.size
        advanceTimeBy(IDLE_PERIOD_MS)
        runCurrent()
        assertEquals(selects, fixture.repository.selectCount)
        assertEquals(reports, fixture.reportedBatches.size)
    }
}

private class Fixture(
    scope: CoroutineScope,
) {
    val repository = InMemoryMessageReceiptRepository()
    val reportedBatches = mutableListOf<List<Message>>()
    private val api = mock<ChatApi>()
    val reporter = MessageReceiptReporter(
        scope = scope,
        messageReceiptRepository = repository,
        api = api,
    )
    private var remainingFailures = 0
    private var deliveryGate: DeliveryGate? = null

    init {
        whenever(api.markDelivered(any())).thenAnswer { invocation ->
            reportedBatches += invocation.getArgument<List<Message>>(0)
            val gate = deliveryGate
            if (gate != null) {
                deliveryGate = null
                gate
            } else if (remainingFailures > 0) {
                remainingFailures -= 1
                DeliveryFailure.asCall()
            } else {
                Unit.asCall()
            }
        }
    }

    fun persist(receipts: List<MessageReceipt>) = apply {
        repository.persist(receipts)
    }

    fun failDeliveries(times: Int) = apply {
        remainingFailures = times
    }

    fun armDeliveryGate(): DeliveryGate = DeliveryGate().also { deliveryGate = it }

    fun assertReportedIds(vararg batches: List<String>) {
        assertEquals(batches.toList(), reportedBatches.map { batch -> batch.map(Message::id) })
    }

    fun connectManager(): ConnectedReceipts {
        val currentUser = randomUser(
            privacySettings = PrivacySettings(
                deliveryReceipts = DeliveryReceipts(enabled = true),
            ),
        )
        val message = randomMessage(
            createdLocallyAt = Date(5_000),
            deletedAt = null,
            parentId = null,
            showInChannel = true,
            shadowed = false,
        )
        val channel = randomChannel(
            read = listOf(
                randomChannelUserRead(
                    user = currentUser,
                    lastRead = NEVER,
                    lastDeliveredAt = null,
                ),
            ),
        )
        val facade = mock<RepositoryFacade> {
            onBlocking { selectUser("me") } doReturn currentUser
            onBlocking { selectChannel(message.cid) } doReturn channel
            onBlocking { selectMessage(message.id) } doReturn message
        }
        val manager = MessageReceiptManager(
            now = { Date(1) },
            getRepositoryFacade = { facade },
            messageReceiptRepository = repository,
            api = mock(),
            onReceiptsEnqueued = reporter::onReceiptsEnqueued,
        )
        return ConnectedReceipts(
            manager = manager,
            message = message,
            currentUser = currentUser,
        )
    }
}

private class ConnectedReceipts(
    val manager: MessageReceiptManager,
    val message: Message,
    val currentUser: User,
)

private class InMemoryMessageReceiptRepository : MessageReceiptRepository {
    private val receipts = mutableListOf<MessageReceipt>()
    val selectLimits = mutableListOf<Int>()
    val selectCount: Int
        get() = selectLimits.size
    var yieldBeforePersist: Boolean = false
    var failNextUpsert: Boolean = false
    private var selectGate: SelectGate? = null

    fun persist(items: List<MessageReceipt>) {
        receipts.addAll(items)
    }

    fun armSelectGate(returnEmpty: Boolean): SelectGate = SelectGate(returnEmpty).also { selectGate = it }

    fun snapshot(): List<MessageReceipt> = receipts.toList()

    override suspend fun upsertMessageReceipts(receipts: List<MessageReceipt>) {
        if (yieldBeforePersist) {
            yield()
        }
        if (failNextUpsert) {
            failNextUpsert = false
            throw IllegalStateException("upsert failed")
        }
        this.receipts.addAll(receipts)
    }

    override suspend fun selectMessageReceipts(limit: Int): List<MessageReceipt> {
        selectLimits += limit
        val gate = selectGate
        if (gate != null) {
            selectGate = null
            if (gate.awaitAndReturnEmpty()) {
                return emptyList()
            }
        }
        return receipts.sortedBy { it.createdAt.time }.take(limit).toList()
    }

    override suspend fun deleteMessageReceiptsByMessageIds(messageIds: List<String>) {
        val ids = messageIds.toSet()
        receipts.removeAll { it.messageId in ids }
    }

    override suspend fun clearMessageReceipts() {
        receipts.clear()
    }
}

private class SelectGate(
    private val returnEmpty: Boolean,
) {
    val entered = CompletableDeferred<Unit>()
    val release = CompletableDeferred<Unit>()

    suspend fun awaitAndReturnEmpty(): Boolean {
        entered.complete(Unit)
        release.await()
        return returnEmpty
    }
}

private class DeliveryGate : Call<Unit> {
    val entered = CompletableDeferred<Unit>()
    private val result = CompletableDeferred<Result<Unit>>()

    override fun cancel() = Unit

    override fun enqueue(callback: Call.Callback<Unit>) {
        error("MessageReceiptReporter awaits delivery instead of enqueueing it")
    }

    override fun execute(): Result<Unit> = error("MessageReceiptReporter awaits delivery instead of executing it")

    override suspend fun await(): Result<Unit> {
        entered.complete(Unit)
        return result.await()
    }

    fun succeed() {
        result.complete(Result.Success(Unit))
    }
}

private fun receipt(index: Int, cid: String = "messaging:general") = MessageReceipt(
    messageId = "m${index.toString().padStart(3, '0')}",
    cid = cid,
    createdAt = Date(index.toLong()),
)

private val DeliveryFailure = Error.GenericError("delivery failed")

private const val REPORT_INTERVAL_MS = 1_000L
private const val IDLE_PERIOD_MS = 60_000L
private const val BATCH_LIMIT = 100
private const val QUEUE_SIZE = 250
private const val NOTIFICATION_BURST = 30
private const val SIGNAL_BURST = 1_000
