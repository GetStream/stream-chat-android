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
import io.getstream.chat.android.models.Message
import io.getstream.log.taggedLogger
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reports message delivery receipts to the server.
 *
 * Idle sessions do not poll the repository. [onReceiptsEnqueued] wakes reporting after receipts
 * are persisted, and each [start] still drains receipts that were saved before the job began.
 * See [start] for batching, pacing, and retry behavior.
 */
internal class MessageReceiptReporter(
    private val scope: CoroutineScope,
    private val messageReceiptRepository: MessageReceiptRepository,
    private val api: ChatApi,
) {

    private val logger by taggedLogger("Chat:MessageReceiptReporter")

    /**
     * Conflated wake-ups. [Channel.trySend] never suspends the caller, and a burst keeps only the
     * latest signal. The channel stays open when a reporting job ends so a later [start] can drain
     * again. Signals carry no payload; the repository remains the source of truth.
     */
    private val enqueueSignals = Channel<Unit>(Channel.CONFLATED)

    private var reportingJob: Job? = null

    /**
     * Starts reporting queued delivery receipts for the current user session.
     *
     * Receipts persisted before this call are selected immediately. While the queue is non-empty,
     * batches of at most [MAX_BATCH_SIZE] are reported at least [REPORT_INTERVAL_IN_MS] apart,
     * including the selection that finds the queue empty. An empty selection suspends until
     * [onReceiptsEnqueued]. Failed deliveries stay queued and are retried on that cadence without
     * another signal. A second call while the reporting job is still active does nothing.
     * Cancelling the job does not close [enqueueSignals]; the next [start] drains again.
     */
    fun start() {
        if (reportingJob?.isActive == true) {
            logger.d { "Reporter is already active" }
            return
        }
        logger.d { "Starting reporter…" }
        reportingJob = scope.launch {
            try {
                drainQueuedReceipts()
                while (true) {
                    enqueueSignals.receive()
                    drainQueuedReceipts()
                }
            } finally {
                logger.d { "Reporter is no longer active" }
            }
        }
    }

    /**
     * Signals that new delivery receipts were persisted.
     *
     * Non-blocking and conflated: the caller does not wait for network I/O, and bursts collapse
     * to a single wake-up.
     */
    fun onReceiptsEnqueued() {
        enqueueSignals.trySend(Unit)
    }

    /**
     * Selects and reports until the repository returns an empty batch.
     *
     * A pending signal is consumed before each selection. A signal that arrives during that
     * selection stays buffered, so an enqueue racing an empty result remains visible to the
     * caller. A stale signal may cause one extra selection and never a periodic idle query.
     * Cancellation from [delay] or [ChatApi.markDelivered] propagates to the reporting job.
     */
    private suspend fun drainQueuedReceipts() {
        while (true) {
            enqueueSignals.tryReceive()

            val messages = messageReceiptRepository
                .selectMessageReceipts(limit = MAX_BATCH_SIZE)
                .map { receipt ->
                    Message(
                        id = receipt.messageId,
                        cid = receipt.cid,
                    )
                }

            if (messages.isEmpty()) {
                return
            }

            logger.d { "Reporting delivery receipts for ${messages.size} messages…" }
            api.markDelivered(messages)
                .await()
                .onSuccessSuspend {
                    logger.d { "Successfully reported delivery receipts for ${messages.size} messages" }
                    val deliveredMessageIds = messages.map(Message::id)
                    messageReceiptRepository.deleteMessageReceiptsByMessageIds(deliveredMessageIds)
                }
                .onError { error ->
                    logger.e {
                        "Failed to report delivery receipts for ${messages.size} messages: " +
                            error.message
                    }
                }

            delay(REPORT_INTERVAL_IN_MS)
        }
    }
}

private const val REPORT_INTERVAL_IN_MS = 1000L
private const val MAX_BATCH_SIZE = 100
