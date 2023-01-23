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
    private val cacheSize: Int = 100,
    private var messageCache: LruCache<String, Message> = LruCache(cacheSize),
) : MessageRepository {
    // the message cache, specifically caches messages on which we're receiving events
    // (saving a few trips to the db when you get 10 likes on 1 message)

    /**
     * Select messages for a channel in a desired page.
     *
     * @param cid String.
     * @param pagination [AnyChannelPaginationRequest]
     */
    override suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message> {
        return selectMessagesEntitiesForChannel(cid, pagination)
            .map { it.toModel(getUser, ::selectRepliedMessage) }
            .filterReactions()
    }

    /**
     * Select messages for a thread in a desired page.
     *
     * @param messageId String.
     * @param limit limit of messages
     */
    override suspend fun selectMessagesForThread(messageId: String, limit: Int): List<Message> {
        return messageDao.messagesForThread(messageId, limit)
            .map { it.toModel(getUser, ::selectRepliedMessage) }
            .filterReactions()
    }

    override suspend fun selectRepliedMessage(messageId: String): Message? {
        return replyMessageDao.selectById(messageId)?.toModel(getUser)
    }

    /**
     * Selects messages by IDs.
     *
     * @param messageIds A list of [Message.id] as query specification.
     * @param forceCache A boolean flag that forces cache in repository and fetches data directly in database if passed
     * value is true.
     *
     * @return A list of messages found in repository.
     */
    override suspend fun selectMessages(messageIds: List<String>, forceCache: Boolean): List<Message> {
        return if (forceCache) {
            fetchMessages(messageIds)
        } else {
            val missingMessageIds = messageIds.filter { messageCache.get(it) == null }
            val cachedIds = messageIds - missingMessageIds.toSet()
            cachedIds.mapNotNull { messageCache[it] } + fetchMessages(missingMessageIds)
        }
    }

    /**
     * Reads the message with passed ID.
     *
     * @param messageId String.
     */
    override suspend fun selectMessage(messageId: String): Message? {
        return messageCache[messageId] ?: messageDao.select(messageId)?.toModel(getUser, ::selectRepliedMessage)
            ?.filterReactions()
            ?.also { messageCache.put(it.id, it) }
    }

    /**
     * Inserts many messages.
     *
     * @param messages list of [Message]
     * @param cache Boolean.
     */
    override suspend fun insertMessages(messages: List<Message>, cache: Boolean) {
        if (messages.isEmpty()) return

        val validMessages = messages.filter { message -> message.cid.isNotEmpty() }

        //Insert messages.
        validMessages.onEach { message ->
            if (messageCache.get(message.id) != null || cache) {
                messageCache.put(message.id, message)
            }
        }
            .map { message -> message.toEntity() }
            .let { entityMessages -> messageDao.insert(entityMessages) }

        //Insert all replies
        validMessages.mapNotNull { message -> message.replyTo }
            .map { message -> message.toReplyEntity() }
            .let(replyMessageDao::insert)
    }

    /**
     * Inserts a messages.
     *
     * @param message [Message]
     * @param cache Boolean.
     */
    override suspend fun insertMessage(message: Message, cache: Boolean) {
        insertMessages(listOf(message), cache)
    }

    /**
     * Deletes all messages before a message with passed ID.
     *
     * @param cid of message - String.
     * @param hideMessagesBefore Boolean.
     */
    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        // delete the messages
        messageDao.deleteChannelMessagesBefore(cid, hideMessagesBefore)
        // wipe the cache
        messageCache = LruCache(cacheSize)
    }

    /**
     * Deletes message.
     *
     * @param message [Message]
     */
    override suspend fun deleteChannelMessage(message: Message) {
        messageDao.deleteMessage(message.cid, message.id)
        messageCache.remove(message.id)
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
        messageDao.deleteAll()
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
    private suspend fun fetchMessages(messageIds: List<String>): List<Message> {
        return messageDao.select(messageIds)
            .map { entity ->
                entity.toModel(getUser, ::selectRepliedMessage).filterReactions().also { messageCache.put(it.id, it) }
            }
    }

    private fun List<Message>.filterReactions(): List<Message> = also {
        forEach { it.filterReactions() }
    }

    /**
     * Workaround to remove reactions which should not be displayed in the UI. This filtering
     * should be done in `MessageDao`.
     */
    private fun Message.filterReactions(): Message = also {
        if (ownReactions.isNotEmpty()) {
            ownReactions = ownReactions
                .filter { it.deletedAt == null }
                .filter { currentUser == null || it.userId == currentUser.id }
                .toMutableList()
        }
        if (latestReactions.isNotEmpty()) {
            latestReactions = latestReactions
                .filter { it.deletedAt == null }
                .toMutableList()
        }
    }

    private companion object {
        private const val DEFAULT_MESSAGE_LIMIT = 100
    }
}
