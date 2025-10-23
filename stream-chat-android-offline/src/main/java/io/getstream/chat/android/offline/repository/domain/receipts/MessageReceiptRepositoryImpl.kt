package io.getstream.chat.android.offline.repository.domain.receipts

import io.getstream.chat.android.client.persistance.repository.MessageReceiptRepository
import io.getstream.chat.android.models.MessageReceipt

internal class MessageReceiptRepositoryImpl(
    private val dao: MessageReceiptDao,
) : MessageReceiptRepository {

    override suspend fun upsert(receipts: List<MessageReceipt>) {
        dao.upsert(receipts.map(MessageReceipt::toEntity))
    }

    override suspend fun getAllByType(type: String): List<MessageReceipt> =
        dao.selectAllByType(type).map(MessageReceiptEntity::toModel)

    override suspend fun deleteByMessageIds(messageIds: List<String>) {
        dao.deleteByMessageIds(messageIds)
    }
}
