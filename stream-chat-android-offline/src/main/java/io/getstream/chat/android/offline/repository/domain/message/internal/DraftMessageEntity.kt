package io.getstream.chat.android.offline.repository.domain.message.internal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = DraftMessageEntity.DRAFT_MESSAGE_ENTITY_TABLE_NAME,
)
internal data class DraftMessageEntity(
    @PrimaryKey
    val id: String,
    val cid: String,
    val text: String,
    val parentId: String? = null,
    val mentionedUsersIds: List<String> = listOf(),
    val silent: Boolean,
    val showinChannel: Boolean,
    val replyMessageId: String? = null,
    val extraData: Map<String, Any> = mapOf(),
) {
    companion object {
        internal const val DRAFT_MESSAGE_ENTITY_TABLE_NAME = "draft_message_entity"
    }
}