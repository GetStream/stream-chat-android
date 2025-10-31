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

import io.getstream.chat.android.client.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.internal.lastMessage
import io.getstream.chat.android.client.extensions.userRead
import io.getstream.chat.android.client.persistence.repository.MessageReceiptRepository
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Manages message delivery receipts: creating and storing them in the repository
 * for later reporting to the server.
 */
internal class MessageReceiptManager(
    private val scope: CoroutineScope,
    private val now: () -> Date,
    private val getCurrentUser: () -> User?,
    private val messageReceiptRepository: MessageReceiptRepository,
) {

    private val logger by taggedLogger("Chat:MessageReceiptManager")

    /**
     * Request to mark the last undelivered messages in the given channels as delivered.
     *
     * A delivery message candidate is the last non-deleted message in the channel that:
     *
     * - Is not yet marked as read by the current user
     * - Is not yet marked as delivered by the current user
     */
    fun markChannelsAsDelivered(channels: List<Channel>) {
        val deliveredMessageCandidates = channels.mapNotNull(::getUndeliveredMessage)
        markMessagesAsDelivered(messages = deliveredMessageCandidates)
    }

    /**
     * Request to mark the given messages as delivered if delivery receipts are enabled
     * in the current user privacy settings.
     *
     * A message can be marked as delivered only if:
     *
     * - It was not sent by the current user
     * - It is not a system message
     * - It is not deleted
     */
    fun markMessagesAsDelivered(messages: List<Message>) {
        if (messages.isEmpty()) {
            logger.w { "[markMessagesAsDelivered] No receipts to send" }
            return
        }

        logger.d { "[markMessagesAsDelivered] Processing delivery receipts for ${messages.size} messages…" }

        val currentUser = getCurrentUser() ?: run {
            logger.w { "[markMessagesAsDelivered] Cannot send delivery receipts: current user is null" }
            return
        }

        // Check if delivery receipts are enabled for the current user
        if (!currentUser.isDeliveryReceiptsEnabled) {
            logger.w { "[markMessagesAsDelivered] Delivery receipts disabled for user ${currentUser.id}" }
            return
        }

        // Filter out messages that shouldn't have delivery receipts sent
        val filteredMessages = messages.filter { message ->
            shouldSendDeliveryReceipt(currentUserId = currentUser.id, message = message)
        }
        if (filteredMessages.size != messages.size) {
            logger.d {
                "[markMessagesAsDelivered] " +
                    "Skipping delivery receipts for ${messages.size - filteredMessages.size} messages"
            }
        }

        if (filteredMessages.isEmpty()) {
            logger.w { "[markMessagesAsDelivered] No receipts to send" }
            return
        }

        scope.launch {
            val receipts = filteredMessages.map { message -> message.toDeliveryReceipt() }
            messageReceiptRepository.upsertMessageReceipts(receipts)

            logger.d { "[markMessagesAsDelivered] ${filteredMessages.size} delivery receipts upserted" }
        }
    }

    private fun getUndeliveredMessage(channel: Channel): Message? {
        if (!channel.config.deliveryEventsEnabled) {
            logger.w { "[getUndeliveredMessage] Delivery events disabled for channel ${channel.cid}" }
            return null
        }
        val currentUser = getCurrentUser() ?: run {
            logger.w { "[getUndeliveredMessage] Cannot get undelivered message: current user is null" }
            return null
        }
        val userRead = channel.userRead(currentUser.id) ?: return null
        // Get the last non-deleted message in the channel
        val lastMessage = channel.lastMessage ?: return null
        val createdAt = lastMessage.getCreatedAtOrThrow()
        // Check if the last message is already marked as read
        if (createdAt <= userRead.lastRead) return null
        // Check if the last message is already marked as delivered
        if (createdAt <= (userRead.lastDeliveredAt ?: NEVER)) return null

        return lastMessage
    }

    private fun shouldSendDeliveryReceipt(currentUserId: UserId, message: Message): Boolean {
        // Don't send delivery receipts for messages sent by the current user
        if (message.user.id == currentUserId) {
            return false
        }

        // Don't send delivery receipts for system messages
        if (message.type == MessageType.SYSTEM) {
            return false
        }

        // Don't send delivery receipts for deleted messages
        if (message.isDeleted()) {
            return false
        }

        return true
    }

    private fun Message.toDeliveryReceipt() = MessageReceipt(
        messageId = id,
        type = MessageReceipt.TYPE_DELIVERY,
        createdAt = now(),
        cid = cid,
    )
}
