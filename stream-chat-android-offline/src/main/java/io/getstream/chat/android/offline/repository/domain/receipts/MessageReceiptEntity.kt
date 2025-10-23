package io.getstream.chat.android.offline.repository.domain.receipts

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "stream_chat_message_receipt")
internal data class MessageReceiptEntity(
    @PrimaryKey
    val messageId: String,
    val type: String,
    val createdAt: Date,
    val cid: String,
)
