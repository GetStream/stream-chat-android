package io.getstream.chat.android.offline.repository.domain.receipts

import io.getstream.chat.android.models.MessageReceipt

internal fun MessageReceipt.toEntity() = MessageReceiptEntity(
    messageId = messageId,
    type = type,
    createdAt = createdAt,
    cid = cid,
)

internal fun MessageReceiptEntity.toModel() = MessageReceipt(
    messageId = messageId,
    type = type,
    createdAt = createdAt,
    cid = cid,
)
