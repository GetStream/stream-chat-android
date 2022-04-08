package io.getstream.chat.android.client.persistence.repository.inmemory

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistence.repository.MessageRepository
import io.getstream.chat.android.client.query.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

internal class MessageInMemoryRepository: MessageRepository {

    override suspend fun selectMessagesForChannel(
        cid: String,
        pagination: AnyChannelPaginationRequest?,
    ): List<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun selectMessages(messageIds: List<String>, forceCache: Boolean): List<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun selectMessage(messageId: String): Message? {
        TODO("Not yet implemented")
    }

    override suspend fun insertMessages(messages: List<Message>, cache: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun insertMessage(message: Message, cache: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteChannelMessagesBefore(cid: String, hideMessagesBefore: Date) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteChannelMessage(message: Message) {
        TODO("Not yet implemented")
    }

    override suspend fun selectMessageBySyncState(syncStatus: SyncStatus): List<Message> {
        TODO("Not yet implemented")
    }
}
