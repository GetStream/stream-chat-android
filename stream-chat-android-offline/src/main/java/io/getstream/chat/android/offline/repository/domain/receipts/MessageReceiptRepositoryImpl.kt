package io.getstream.chat.android.offline.repository.domain.receipts

import io.getstream.chat.android.client.persistance.repository.MessageReceiptRepository
import io.getstream.chat.android.models.MessageReceipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MessageReceiptRepositoryImpl(
    private val dao: MessageReceiptDao,
) : MessageReceiptRepository {

    override suspend fun upsert(receipts: List<MessageReceipt>) {
        dao.upsert(receipts.map(MessageReceipt::toEntity))
    }

    override fun getAllByType(type: String, limit: Int): Flow<List<MessageReceipt>> =
        dao.selectAllByType(type, limit)
            .map { receipts ->
                receipts.map(MessageReceiptEntity::toModel)
            }

    override suspend fun deleteByMessageIds(messageIds: List<String>) {
        dao.deleteByMessageIds(messageIds)
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
