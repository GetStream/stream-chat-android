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
import io.getstream.chat.android.client.persistence.repository.MessageReceiptRepository
import io.getstream.chat.android.models.Message
import io.getstream.log.taggedLogger
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Reports message delivery receipts to the server in batches of [MAX_BATCH_SIZE]
 * every [REPORT_INTERVAL_IN_MS] milliseconds.
 */
internal class MessageReceiptReporter(
    private val scope: CoroutineScope,
    private val chatClient: ChatClient,
    private val messageReceiptRepository: MessageReceiptRepository,
) {

    private val logger by taggedLogger("Chat:MessageReceiptReporter")

    fun start() {
        logger.d { "Starting reporter…" }
        scope.launch {
            try {
                while (isActive) {
                    val messages = messageReceiptRepository.getAllMessageReceiptsByType(
                        type = MessageReceipt.TYPE_DELIVERY,
                        limit = MAX_BATCH_SIZE,
                    ).map { receipt ->
                        Message(
                            id = receipt.messageId,
                            cid = receipt.cid,
                        )
                    }

                    if (messages.isNotEmpty()) {
                        logger.d { "Reporting delivery receipts for ${messages.size} messages…" }
                        chatClient.markMessagesAsDelivered(messages)
                            .execute()
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
                    }

                    delay(REPORT_INTERVAL_IN_MS)
                }
            } finally {
                logger.d { "Reporter is no longer active" }
            }
        }
    }
}

private const val REPORT_INTERVAL_IN_MS = 1000L
private const val MAX_BATCH_SIZE = 100
