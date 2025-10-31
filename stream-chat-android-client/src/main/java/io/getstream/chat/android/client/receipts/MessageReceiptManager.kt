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

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.internal.lastMessage
import io.getstream.chat.android.client.extensions.userRead
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistence.repository.MessageReceiptRepository
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import java.util.Date

/**
 * Manages message delivery receipts: creating and storing them in the repository
 * for later reporting to the server.
 */
internal class MessageReceiptManager(
    private val now: () -> Date,
    private val getCurrentUser: () -> User?,
    private val repositoryFacade: RepositoryFacade,
    private val messageReceiptRepository: MessageReceiptRepository,
    private val api: ChatApi,
) {

    private val logger by taggedLogger("Chat:MessageReceiptManager")

    /**
     * Request to mark the given channels as delivered
     * if delivery receipts are enabled for the current user.
     *
     * A channel can have a message marked as delivered only if:
     * - Delivery events are enabled in the channel config
     *
     * A delivery message candidate is the last non-deleted message in the channel that:
     * - It was not sent by the current user
     * - Is not shadow banned
     * - Was not sent by a muted user
     * - Is not yet marked as read by the current user
     * - Is not yet marked as delivered by the current user
     */
    suspend fun markChannelsAsDelivered(channels: List<Channel>) {
        val currentUser = getCurrentUser() ?: run {
            logger.w { "[markChannelsAsDelivered] Current user is null" }
            return
        }

        if (!currentUser.isDeliveryReceiptsEnabled) return

        val deliveredMessageCandidates = channels.mapNotNull { channel ->
            channel.lastMessage?.takeIf { lastNonDeletedMessage ->
                canMarkMessageAsDelivered(currentUser, channel, lastNonDeletedMessage)
            }
        }
        markMessagesAsDelivered(messages = deliveredMessageCandidates)
    }

    /**
     * Request to mark the given message as delivered
     * if delivery receipts are enabled for the current user.
     *
     * @see [markChannelsAsDelivered] for the conditions to mark a message as delivered.
     */
    suspend fun markMessageAsDelivered(message: Message) {
        val currentUser = getCurrentUser() ?: run {
            logger.w { "[markMessageAsDelivered] Current user is null" }
            return
        }

        if (!currentUser.isDeliveryReceiptsEnabled) return

        val channel = retrieveChannel(message.cid) ?: run {
            logger.w { "[markMessageAsDelivered] Channel ${message.cid} not found" }
            return
        }

        if (canMarkMessageAsDelivered(currentUser, channel, message)) {
            markMessagesAsDelivered(messages = listOf(message))
        }
    }

    /**
     * Request to mark the message with the given id as delivered.
     *
     * @see [markChannelsAsDelivered] for the conditions to mark a message as delivered.
     */
    suspend fun markMessageAsDelivered(messageId: String) {
        val message = retrieveMessage(messageId) ?: run {
            logger.w { "[markMessageAsDelivered] Message $messageId not found" }
            return
        }

        markMessageAsDelivered(message)
    }

    private suspend fun retrieveChannel(cid: String): Channel? =
        repositoryFacade.selectChannel(cid) ?: run {
            val (channelType, channelId) = cid.cidToTypeAndId()
            val request = QueryChannelRequest()
            api.queryChannel(channelType, channelId, request)
                .await().getOrNull()
        }

    private suspend fun retrieveMessage(id: String): Message? =
        repositoryFacade.selectMessage(id) ?: run {
            api.getMessage(id)
                .await().getOrNull()
        }

    private suspend fun markMessagesAsDelivered(messages: List<Message>) {
        if (messages.isEmpty()) {
            logger.w { "[markMessagesAsDelivered] No receipts to send" }
            return
        }

        logger.d { "[markMessagesAsDelivered] Processing delivery receipts for ${messages.size} messages…" }

        val receipts = messages.map { message -> message.toDeliveryReceipt() }
        messageReceiptRepository.upsertMessageReceipts(receipts)

        logger.d { "[markMessagesAsDelivered] ${messages.size} delivery receipts upserted" }
    }

    private fun canMarkMessageAsDelivered(
        currentUser: User,
        channel: Channel,
        message: Message,
    ): Boolean {
        // Check if delivery events are enabled for the channel
        if (!channel.config.deliveryEventsEnabled) {
            logger.w { "[canMarkMessageAsDelivered] Delivery events disabled for channel ${channel.cid}" }
            return false
        }

        // Do not send delivery receipts for messages sent by the current user
        if (message.user.id == currentUser.id) {
            logger.w {
                "[canMarkMessageAsDelivered] Message ${message.id} was sent by the current user ${currentUser.id}"
            }
            return false
        }

        // Do not send delivery receipts for shadowed messages
        if (message.shadowed) {
            logger.w { "[canMarkMessageAsDelivered] Message ${message.id} is shadowed" }
            return false
        }

        // Do not send delivery receipts for messages sent by muted users
        if (currentUser.mutes.any { mute -> mute.target?.id == message.user.id }) {
            logger.w { "[canMarkMessageAsDelivered] Message ${message.id} was sent by a muted user ${message.user.id}" }
            return false
        }

        val userRead = channel.userRead(currentUser.id) ?: run {
            logger.w {
                "[canMarkMessageAsDelivered] No read state for user ${currentUser.id} in channel ${channel.cid}"
            }
            return false
        }

        val createdAt = message.getCreatedAtOrThrow()

        // Check if the last message is already marked as read
        if (createdAt <= userRead.lastRead) {
            logger.w { "[canMarkMessageAsDelivered] Message ${message.id} is already marked as read" }
            return false
        }

        // Check if the last message is already marked as delivered
        if (createdAt <= (userRead.lastDeliveredAt ?: NEVER)) {
            logger.w { "[canMarkMessageAsDelivered] Message ${message.id} is already marked as delivered" }
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
