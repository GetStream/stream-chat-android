package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.dao.MessageDao
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import java.security.InvalidParameterException

class MessageRepository(var messageDao: MessageDao) {
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
        return messageDao.select(messageId)
    }
    suspend fun select(messageIds: List<String>): List<MessageEntity> {
        return messageDao.select(messageIds)
    }

    suspend fun insertMessages(messages: List<Message>) {
        val messageEntities = mutableListOf<MessageEntity>()
        for (message in messages) {
            if (message.cid == "") {
                throw InvalidParameterException("message.cid cant be empty")
            }
            messageEntities.add(MessageEntity(message))
        }
        messageDao.insertMany(messageEntities)
    }

    suspend fun insertMessage(message: Message) {
        val messageEntity = MessageEntity(message)
        messageDao.insert(messageEntity)
    }

    suspend fun insert(messageEntity: MessageEntity) {
        messageDao.insert(messageEntity)
    }

    suspend fun selectSyncNeeded(): List<MessageEntity> {
        return messageDao.selectSyncNeeded()
    }

}