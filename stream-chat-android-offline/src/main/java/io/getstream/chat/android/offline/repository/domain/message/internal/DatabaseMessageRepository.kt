/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.message.internal

import androidx.collection.LruCache
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import java.util.Date

internal class DatabaseMessageRepository(
    private val messageDao: MessageDao,
    private val replyMessageDao: ReplyMessageDao,
    private val getUser: suspend (userId: String) -> User,
    private val currentUser: User?,
    cacheSize: Int = 1000,
) : MessageRepository {
    // the message cache, specifically caches messages on which we're receiving events
    // (saving a few trips to the db when you get 10 likes on 1 message)

    private val messageCache: LruCache<String, Message> = LruCache(cacheSize)
    private val replyMessageCache: LruCache<String, Message> = LruCache(cacheSize)

    /**
     * Select messages for a channel in a desired page.
     *
     * @param cid String.
     * @param pagination [AnyChannelPaginationRequest]
     */
    override suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message> =
        selectMessagesEntitiesForChannel(cid, pagination)
            .map { it.toMessage() }

    /**
     * Select messages for a thread in a desired page.
     *
     * @param messageId String.
     * @param limit limit of messages
     */
    override suspend fun selectMessagesForThread(messageId: String, limit: Int): List<Message> =
        messageDao.messagesForThread(messageId, limit)
            .map { it.toMessage() }

    private suspend fun selectRepliedMessage(messageId: String): Message? =
        replyMessageCache[messageId] ?: replyMessageDao.selectById(messageId)?.toModel(getUser)

    /**
     * Selects messages by IDs.
     *
     * @param messageIds A list of [Message.id] as query specification.
     *
     * @return A list of messages found in repository.
     */
    override suspend fun selectMessages(messageIds: List<String>): List<Message> {
        return messageIds.map { it to messageCache[it] }
            .partition { it.second != null }
            .let { (cachedMessages, missingMessages) ->
                cachedMessages.mapNotNull { it.second } +
                    (
                        missingMessages.map { it.first }
                            .takeUnless { it.isEmpty() }
                            ?.let { fetchMessagesFromDB(it) }
                            ?: emptyList()
                        )
            }
    }

    /**
     * Reads the message with passed ID.
     *
     * @param messageId String.
     */
    override suspend fun selectMessage(messageId: String): Message? =
        messageCache[messageId] ?: fetchMessageFromDB(messageId)

    /**
     * Inserts many messages.
     *
     * @param messages list of [Message]
     */
    override suspend fun insertMessages(messages: List<Message>) {
        if (messages.isEmpty()) return
        val validMessages = messages
            .filter { message -> message.cid.isNotEmpty() }

        val messagesToInsert = validMessages
            .filter { messageCache.get(it.id) != it }
            .map(Message::toEntity)
        val replyMessages = validMessages
            .mapNotNull { message -> message.replyTo }

        val replyMessagesToInsert = replyMessages
            .filter { replyMessageCache.get(it.id) != it }
            .map(Message::toReplyEntity)

        replyMessages.forEach { replyMessageCache.put(it.id, it) }
        validMessages.forEach { messageCache.put(it.id, it) }
        replyMessagesToInsert.takeUnless { it.isEmpty() }
            ?.let { replyMessageDao.insert(it) }
        messagesToInsert.takeUnless { it.isEmpty() }
            ?.let { messageDao.insert(it) }
    }

    /**
     * Inserts a messages.
     *
     * @param message [Message]
     * @param cache Boolean.
     */
    override suspend fun insertMessage(message: Message) {
        insertMessages(listOf(message))
    }

    /**
     * Deletes all messages before a message with passed ID.
     *
     * @param cid of message - String.
     * @param hideMessagesBefore Boolean.
     */
    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        messageCache.evictAll()
        replyMessageCache.evictAll()
        messageDao.deleteChannelMessagesBefore(cid, hideMessagesBefore)
    }

    /**
     * Deletes message.
     *
     * @param message [Message]
     */
    override suspend fun deleteChannelMessage(message: Message) {
        messageCache.remove(message.id)
        messageDao.deleteMessage(message.cid, message.id)
    }

    /**
     * Selects all message ids of a [SyncStatus]
     *
     * @param syncStatus [SyncStatus]
     */
    override suspend fun selectMessageIdsBySyncState(syncStatus: SyncStatus): List<String> {
        return messageDao.selectIdsBySyncStatus(syncStatus)
    }

    /**
     * Selects all message of a [SyncStatus]
     *
     * @param syncStatus [SyncStatus]
     */
    override suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message> {
        return messageDao.selectBySyncStatus(syncStatus).map { it.toModel(getUser, ::selectRepliedMessage) }
    }

    override suspend fun clear() {
        messageCache.evictAll()
        replyMessageCache.evictAll()
        messageDao.deleteAll()
        replyMessageDao.deleteAll()
    }

    private suspend fun selectMessagesEntitiesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<MessageEntity> {
        val messageFilterDirection = pagination?.messageFilterDirection
        return if (messageFilterDirection != null) {
            // handle the differences between gt, gte, lt and lte
            val message = messageDao.select(pagination.messageFilterValue)
            if (message?.messageInnerEntity?.createdAt == null) return listOf()
            val messageLimit = pagination.messageLimit
            val messageTime = message.messageInnerEntity.createdAt

            when (messageFilterDirection) {
                Pagination.GREATER_THAN_OR_EQUAL -> {
                    messageDao.messagesForChannelEqualOrNewerThan(cid, messageLimit, messageTime)
                }
                Pagination.GREATER_THAN -> {
                    messageDao.messagesForChannelNewerThan(cid, messageLimit, messageTime)
                }
                Pagination.LESS_THAN_OR_EQUAL -> {
                    messageDao.messagesForChannelEqualOrOlderThan(cid, messageLimit, messageTime)
                }
                Pagination.LESS_THAN -> {
                    messageDao.messagesForChannelOlderThan(cid, messageLimit, messageTime)
                }
                Pagination.AROUND_ID -> emptyList()
            }
        } else {
            messageDao.messagesForChannel(cid, pagination?.messageLimit ?: DEFAULT_MESSAGE_LIMIT)
        }
    }

    /** Fetches messages from [MessageDao] and cache values in [LruCache]. */
    private suspend fun fetchMessagesFromDB(messageIds: List<String>): List<Message> {
        return messageDao.select(messageIds)
            .map { entity ->
                entity.toMessage()
                    .also { messageCache.put(it.id, it) }
            }
    }

    private suspend fun fetchMessageFromDB(messageId: String): Message? {
        return messageDao.select(messageId)
            ?.toMessage()
            ?.also { messageCache.put(it.id, it) }
    }

    private suspend fun MessageEntity.toMessage(): Message =
        this.toModel(getUser, ::selectRepliedMessage).filterReactions()

    /**
     * Workaround to remove reactions which should not be displayed in the UI. This filtering
     * should be done in `MessageDao`.
     */
    private fun Message.filterReactions(): Message = copy(
        ownReactions = ownReactions
            .filter { it.deletedAt == null }
            .filter { currentUser == null || it.userId == currentUser.id },
        latestReactions = latestReactions.filter { it.deletedAt == null },
    )

    private companion object {
        private const val DEFAULT_MESSAGE_LIMIT = 100
    }
}
