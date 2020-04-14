package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.dao.MessageDao
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import java.security.InvalidParameterException

class MessageRepository(var messageDao: MessageDao, var cacheSize: Int = 100) {
    // the message cache, specifically caches messages on which we're receiving events (saving a few trips to the db when you get 10 likes on 1 message)
    var messageCache = LruCache<String, MessageEntity>(cacheSize)

    internal suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest
    ): List<MessageEntity> {

        // - fetch the message you are filtering on and get it's date
        // - sort asc or desc based on filter direction
        var sort = "ASC"
        if (pagination.isFilteringOlderMessages()) {
            sort = "DESC"
        }
        if (pagination.hasFilter()) {
            // TODO: this doesn't support the difference between gte vs gt
            val message = messageDao.select(pagination.messageFilterValue)
            if (message?.createdAt == null) {
                return listOf()
            } else if (pagination.isFilteringNewerMessages()) {
                return messageDao.messagesForChannelNewerThan(cid, pagination.messageLimit, message.createdAt!!)
            } else if (pagination.isFilteringOlderMessages()) {
                return messageDao.messagesForChannelOlderThan(cid, pagination.messageLimit, message.createdAt!!)

            }

        }

        return messageDao.messagesForChannel(cid, pagination.messageLimit)
    }

    suspend fun selectMessageEntity(messageId: String): MessageEntity? {
        return select(listOf(messageId)).getOrElse(0) {null}
    }
    suspend fun select(messageIds: List<String>): List<MessageEntity> {
        val cachedMessages: MutableList<MessageEntity> = mutableListOf()
        for (messageId in messageIds) {
            val messageEntity = messageCache.get(messageId)
            messageEntity?.let { cachedMessages.add(it) }
        }
        val missingMessageIds = messageIds.filter { messageCache.get(it) == null }
        val dbMessages = messageDao.select(missingMessageIds).toMutableList()

        dbMessages.addAll(cachedMessages)
        return dbMessages
    }

    suspend fun insertMessageEntities(messageEntities: List<MessageEntity>, cache: Boolean=false) {
        if (messageEntities.isEmpty()) return
        for (messageEntity in messageEntities) {
            if (messageEntity.cid == "") {
                throw InvalidParameterException("message.cid cant be empty")
            }
        }
        messageDao.insertMany(messageEntities)
    }

    suspend fun insertMessages(messages: List<Message>) {
        val messageEntities = messages.map { MessageEntity(it) }
        insertMessageEntities(messageEntities)
    }

    suspend fun insertMessage(message: Message) {
        val messageEntity = MessageEntity(message)
        insertMessageEntities(listOf(messageEntity))
    }

    suspend fun insert(messageEntity: MessageEntity) {
        insertMessageEntities(listOf(messageEntity))
    }

    suspend fun selectSyncNeeded(): List<MessageEntity> {
        return messageDao.selectSyncNeeded()
    }

}