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
import io.getstream.log.taggedLogger
import io.getstream.result.onSuccessSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample

/**
 * Reports message delivery receipts to the server in batches of [MAX_BATCH_SIZE]
 * every [REPORT_INTERVAL_IN_MS] milliseconds.
 */
internal class MessageReceiptReporter(
    private val scope: CoroutineScope,
    private val chatClient: ChatClient,
    private val messageReceiptRepository: MessageReceiptRepository,
) {

    private val logger by taggedLogger("MessageReceiptReporter")

    @OptIn(FlowPreview::class)
    fun init() {
        messageReceiptRepository.getAllByType(type = MessageReceipt.TYPE_DELIVERY, limit = MAX_BATCH_SIZE)
            .sample(REPORT_INTERVAL_IN_MS)
            .filterNot(List<MessageReceipt>::isEmpty)
            .map { receipts ->
                receipts.map { receipt ->
                    Message(
                        id = receipt.messageId,
                        cid = receipt.cid,
                    )
                }
            }
            .onEach { messages ->
                logger.d { "[init] Reporting delivery receipts for ${messages.size} messages…" }
                chatClient.markMessagesAsDelivered(messages)
                    .execute()
                    .onSuccessSuspend {
                        val deliveredMessageIds = messages.map(Message::id)
                        messageReceiptRepository.deleteByMessageIds(deliveredMessageIds)
                    }
            }
            .launchIn(scope)
    }
}

private const val REPORT_INTERVAL_IN_MS = 1000L
private const val MAX_BATCH_SIZE = 100
