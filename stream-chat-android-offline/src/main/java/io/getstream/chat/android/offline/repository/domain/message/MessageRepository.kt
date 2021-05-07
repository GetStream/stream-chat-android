package io.getstream.chat.android.offline.repository.domain.message

import androidx.collection.LruCache
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.hasFilter
import java.util.Date

internal interface MessageRepository {
    suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message>

    suspend fun selectMessages(messageIds: List<String>): List<Message>
    suspend fun selectMessage(messageId: String): Message?
    suspend fun insertMessages(messages: List<Message>, cache: Boolean = false)
    suspend fun insertMessage(message: Message, cache: Boolean = false)
    suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date)
    suspend fun deleteChannelMessage(message: Message)
    suspend fun selectMessagesSyncNeeded(): List<Message>
}

internal class MessageRepositoryImpl(
    private val messageDao: MessageDao,
    private val getUser: suspend (userId: String) -> User,
    private val cacheSize: Int = 100,
    private var messageCache: LruCache<String, Message> = LruCache(cacheSize)
) : MessageRepository {
    // the message cache, specifically caches messages on which we're receiving events (saving a few trips to the db when you get 10 likes on 1 message)

    override suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message> {
        return selectMessagesEntitiesForChannel(cid, pagination).map { it.toModel(getUser, ::selectMessage) }
    }

    private suspend fun selectMessagesEntitiesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<MessageEntity> {
        if (pagination != null && pagination.hasFilter()) {
            // handle the differences between gt, gte, lt and lte
            val message = messageDao.select(pagination.messageFilterValue)
            if (message?.messageInnerEntity?.createdAt == null) return listOf()
            val messageLimit = pagination.messageLimit
            val messageTime = message.messageInnerEntity.createdAt

            when (pagination.messageFilterDirection) {
                Pagination.GREATER_THAN_OR_EQUAL -> {
                    return messageDao.messagesForChannelEqualOrNewerThan(cid, messageLimit, messageTime)
                }
                Pagination.GREATER_THAN -> {
                    return messageDao.messagesForChannelNewerThan(cid, messageLimit, messageTime)
                }
                Pagination.LESS_THAN_OR_EQUAL -> {
                    return messageDao.messagesForChannelEqualOrOlderThan(cid, messageLimit, messageTime)
                }
                Pagination.LESS_THAN -> {
                    return messageDao.messagesForChannelOlderThan(cid, messageLimit, messageTime)
                }
            }
        }
        return messageDao.messagesForChannel(cid, pagination?.messageLimit ?: DEFAULT_MESSAGE_LIMIT)
    }

    override suspend fun selectMessages(messageIds: List<String>): List<Message> {
        val missingMessageIds = messageIds.filter { messageCache.get(it) == null }
        val cachedIds = messageIds - missingMessageIds
        return cachedIds.mapNotNull { messageCache[it] } + messageDao.select(missingMessageIds)
            .map { entity -> entity.toModel(getUser, ::selectMessage).also { messageCache.put(it.id, it) } }
    }

    override suspend fun selectMessage(messageId: String): Message? {
        return messageCache[messageId] ?: messageDao.select(messageId)?.toModel(getUser, ::selectMessage)
            ?.also { messageCache.put(it.id, it) }
    }

    override suspend fun insertMessages(messages: List<Message>, cache: Boolean) {
        if (messages.isEmpty()) return
        val messagesToInsert = messages.flatMap(Companion::allMessages)
        for (message in messagesToInsert) {
            require(message.cid.isNotEmpty()) { "message.cid can not be empty" }
        }
        for (m in messagesToInsert) {
            if (messageCache.get(m.id) != null || cache) {
                messageCache.put(m.id, m)
            }
        }
        messageDao.insert(messagesToInsert.map { it.toEntity() })
    }

    override suspend fun insertMessage(message: Message, cache: Boolean) {
        insertMessages(listOf(message), cache)
    }

    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        // delete the messages
        messageDao.deleteChannelMessagesBefore(cid, hideMessagesBefore)
        // wipe the cache
        messageCache = LruCache(cacheSize)
    }

    override suspend fun deleteChannelMessage(message: Message) {
        messageDao.deleteMessage(message.cid, message.id)
        messageCache.remove(message.id)
    }

    override suspend fun selectMessagesSyncNeeded(): List<Message> {
        return messageDao.selectSyncNeeded().map { it.toModel(getUser, ::selectMessage) }
    }

    private companion object {
        private const val DEFAULT_MESSAGE_LIMIT = 100

        private fun allMessages(message: Message): List<Message> =
            listOf(message) + (message.replyTo?.let(Companion::allMessages).orEmpty())
    }
}
