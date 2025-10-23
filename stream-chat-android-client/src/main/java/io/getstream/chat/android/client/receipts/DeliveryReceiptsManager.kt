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
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.log.taggedLogger

internal class DeliveryReceiptsManager(
    private val chatClient: ChatClient,
    private val getCurrentUser: () -> User?,
) {

    private val logger by taggedLogger("MessageDeliveryReceiptsManager")

    fun markMessagesAsDelivered(messages: List<Message>) {
        logger.d { "[markMessagesAsDelivered] Preparing to send delivery receipts for ${messages.size} messages" }

        val currentUser = requireNotNull(getCurrentUser()) {
            "Cannot send delivery receipts: current user is null"
        }

        // Check if delivery receipts are enabled for the current user
        if (!currentUser.isDeliveryReceiptsEnabled()) {
            logger.w { "[markMessagesAsDelivered] Delivery receipts disabled for user ${currentUser.id}" }
            return
        }

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

        logger.d { "[markMessagesAsDelivered] Sending ${filteredMessages.size} delivery receipts" }
        chatClient.markMessagesAsDelivered(filteredMessages)
            .execute()
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
}

private fun User.isDeliveryReceiptsEnabled(): Boolean =
    privacySettings?.deliveryReceipts?.enabled ?: false
