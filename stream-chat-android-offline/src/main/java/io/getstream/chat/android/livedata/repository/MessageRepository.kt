package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import androidx.collection.LruCache
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.MessageDao
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.request.hasFilter
import java.util.Date

internal class MessageRepository(
    private val messageDao: MessageDao,
    private val cacheSize: Int = 100
) {
    // the message cache, specifically caches messages on which we're receiving events (saving a few trips to the db when you get 10 likes on 1 message)
    @VisibleForTesting
    internal var messageCache = LruCache<String, Message>(cacheSize)
        private set

    internal suspend fun selectMessagesForChannel(
        cid: String,
        usersMap: Map<String, User>,
        pagination: AnyChannelPaginationRequest
    ): List<Message> {
        return selectMessagesEntitiesForChannel(cid, pagination).map { toModel(it, usersMap) }
    }

    internal suspend fun selectMessagesEntitiesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?
    ): List<MessageEntity> {
        if (pagination != null && pagination.hasFilter()) {
            // handle the differences between gt, gte, lt and lte
            val message = messageDao.select(pagination.messageFilterValue)
            if (message?.createdAt == null) return listOf()
            val messageLimit = pagination.messageLimit
            val messageTime = message.createdAt

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

    internal suspend fun selectMessagesEntitiesForChannels(
        cids: List<String>,
        pagination: AnyChannelPaginationRequest?
    ): Map<String, List<MessageEntity>> {
        if (pagination != null && pagination.hasFilter()) {
            // handle the differences between gt, gte, lt and lte
            val message = messageDao.select(pagination.messageFilterValue)
            if (message?.createdAt == null) return emptyMap()
            val messageLimit = pagination.messageLimit
            val messageTime = message.createdAt

            when (pagination.messageFilterDirection) {
                else -> return TODO()
                /*Pagination.GREATER_THAN_OR_EQUAL -> {
                    return messageDao.messagesForChannelEqualOrNewerThan(cids, messageLimit, messageTime)
                }
                Pagination.GREATER_THAN -> {
                    return messageDao.messagesForChannelNewerThan(cids, messageLimit, messageTime)
                }
                Pagination.LESS_THAN_OR_EQUAL -> {
                    return messageDao.messagesForChannelEqualOrOlderThan(cids, messageLimit, messageTime)
                }
                Pagination.LESS_THAN -> {
                    return messageDao.messagesForChannelOlderThan(cids, messageLimit, messageTime)
                }*/
            }
        }
        return messageDao.messagesForChannel(cids, pagination?.messageLimit ?: DEFAULT_MESSAGE_LIMIT)
    }

    suspend fun select(messageIds: List<String>, usersMap: Map<String, User>): List<Message> {
        val cachedMessages: MutableList<Message> = mutableListOf()
        for (messageId in messageIds) {
            val cachedMessage = messageCache.get(messageId)
            cachedMessage?.let { cachedMessages.add(it) }
        }
        val missingMessageIds = messageIds.filter { messageCache.get(it) == null }
        val dbMessages = messageDao.select(missingMessageIds).map { toModel(it, usersMap) }.toMutableList()

        dbMessages.addAll(cachedMessages)
        return dbMessages
    }

    /**
     * Shouldn't be exposed to business logic. It could be used only by the Repository layer (RepositoryHelper).
     */
    internal suspend fun selectEntities(messageIds: List<String>): List<MessageEntity> {
        val cachedEntities = messageIds.fold(emptyList<MessageEntity>()) { acc, id ->
            val cachedMessage = messageCache[id]
            if (cachedMessage != null) {
                acc + toEntity(cachedMessage)
            } else {
                acc
            }
        }
        val missingIds: List<String> = messageIds - cachedEntities.map(MessageEntity::id)
        return messageDao.select(missingIds) + cachedEntities
    }

    suspend fun select(messageId: String, usersMap: Map<String, User>): Message? {
        return messageCache[messageId] ?: messageDao.select(messageId)?.let { toModel(it, usersMap) }
    }

    suspend fun insert(messages: List<Message>, cache: Boolean = false) {
        if (messages.isEmpty()) return
        for (message in messages) {
            require(message.cid.isNotEmpty()) { "message.cid can not be empty" }
        }
        for (m in messages) {
            if (messageCache.get(m.id) != null || cache) {
                messageCache.put(m.id, m)
            }
        }
        messageDao.insertMany(messages.map { toEntity(it) })
    }

    suspend fun insert(message: Message, cache: Boolean = false) {
        insert(listOf(message), cache)
    }

    suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        // delete the messages
        messageDao.deleteChannelMessagesBefore(cid, hideMessagesBefore)
        // wipe the cache
        messageCache = LruCache(cacheSize)
    }

    suspend fun deleteChannelMessage(message: Message) {
        messageDao.deleteMessage(message.cid, message.id)
        messageCache.remove(message.id)
    }

    internal suspend fun selectSyncNeeded(userMap: Map<String, User>): List<Message> {
        return messageDao.selectSyncNeeded().map { toModel(it, userMap) }
    }

    companion object {
        private const val DEFAULT_MESSAGE_LIMIT = 100

        internal fun toModel(entity: MessageEntity, userMap: Map<String, User>): Message = with(entity) {
            val message = Message()
            message.id = id
            message.cid = cid
            message.user = userMap[userId]
                ?: error("userMap doesnt contain user id $userId for message id ${message.id}")
            message.text = text
            message.attachments = attachments.toMutableList()
            message.type = type
            message.replyCount = replyCount
            message.createdAt = createdAt
            message.createdLocallyAt = createdLocallyAt
            message.updatedAt = updatedAt
            message.updatedLocallyAt = updatedLocallyAt
            message.deletedAt = deletedAt
            message.parentId = parentId
            message.command = command
            message.extraData = extraData.toMutableMap()
            message.reactionCounts = reactionCounts.toMutableMap()
            message.reactionScores = reactionScores.toMutableMap()
            message.syncStatus = syncStatus

            message.latestReactions = (latestReactions.map { it.toReaction(userMap) }).toMutableList()
            message.ownReactions = (ownReactions.map { it.toReaction(userMap) }).toMutableList()
            message.mentionedUsers = mentionedUsersId.mapNotNull { userMap[it] }.toMutableList()

            message
        }

        internal fun toEntity(model: Message) = MessageEntity(
            id = model.id, cid = model.cid, userId = model.user.id,
            text = model.text,
            attachments = model.attachments,
            syncStatus = model.syncStatus,
            type = model.type,
            replyCount = model.replyCount,
            createdAt = model.createdAt,
            createdLocallyAt = model.createdLocallyAt,
            updatedAt = model.updatedAt,
            updatedLocallyAt = model.updatedLocallyAt,
            deletedAt = model.deletedAt,
            parentId = model.parentId,
            command = model.command,
            extraData = model.extraData,
            reactionCounts = model.reactionCounts,
            reactionScores = model.reactionScores,
            // for these we need a little map,
            latestReactions = model.latestReactions.map(::ReactionEntity),
            ownReactions = model.ownReactions.map(::ReactionEntity),
            mentionedUsersId = model.mentionedUsers.map(User::id)
        )
    }
}
