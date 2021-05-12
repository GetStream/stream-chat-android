package io.getstream.chat.android.offline.repository.domain.message

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.domain.message.attachment.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.reaction.ReactionEntity
import java.util.Date

@Dao
internal abstract class MessageDao {

    @Transaction
    open suspend fun insert(messageEntities: List<MessageEntity>) {
        upsertMessageInnerEntities(messageEntities.map(MessageEntity::messageInnerEntity))
        insertAttachments(messageEntities.flatMap(MessageEntity::attachments))
        insertReactions(messageEntities.flatMap { it.latestReactions + it.ownReactions })
    }

    @Transaction
    open suspend fun insert(messageEntity: MessageEntity) {
        upsertMessageInnerEntity(messageEntity.messageInnerEntity)
        insertAttachments(messageEntity.attachments)
        insertReactions(messageEntity.let { it.latestReactions + it.ownReactions })
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertMessageInnerEntity(messageInnerEntity: MessageInnerEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertMessageInnerEntities(messageInnerEntities: List<MessageInnerEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun updateMessageInnerEntity(messageInnerEntity: MessageInnerEntity)

    @Transaction
    open suspend fun upsertMessageInnerEntity(messageInnerEntity: MessageInnerEntity) {
        val rowId = insertMessageInnerEntity(messageInnerEntity)
        if (rowId == -1L) {
            updateMessageInnerEntity(messageInnerEntity)
        }
    }

    @Transaction
    open suspend fun upsertMessageInnerEntities(messageInnerEntities: List<MessageInnerEntity>) {
        val rowIds = insertMessageInnerEntities(messageInnerEntities)
        val entitiesToUpdate = rowIds.mapIndexedNotNull { index, rowId ->
            if (rowId == -1L) null else messageInnerEntities[index]
        }
        entitiesToUpdate.forEach { updateMessageInnerEntity(it) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAttachments(attachmentEntities: List<AttachmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertReactions(reactions: List<ReactionEntity>)

    @Query("SELECT * from stream_chat_message WHERE cid = :cid AND createdAt > :dateFilter ORDER BY createdAt ASC LIMIT :limit")
    @Transaction
    abstract suspend fun messagesForChannelNewerThan(
        cid: String,
        limit: Int = 100,
        dateFilter: Date,
    ): List<MessageEntity>

    @Query("SELECT * from stream_chat_message WHERE cid = :cid AND createdAt >= :dateFilter ORDER BY createdAt ASC LIMIT :limit")
    @Transaction
    abstract suspend fun messagesForChannelEqualOrNewerThan(
        cid: String,
        limit: Int = 100,
        dateFilter: Date,
    ): List<MessageEntity>

    @Query("SELECT * from stream_chat_message WHERE cid = :cid AND createdAt < :dateFilter ORDER BY createdAt DESC LIMIT :limit")
    @Transaction
    abstract suspend fun messagesForChannelOlderThan(
        cid: String,
        limit: Int = 100,
        dateFilter: Date,
    ): List<MessageEntity>

    @Query("SELECT * from stream_chat_message WHERE cid = :cid AND createdAt <= :dateFilter ORDER BY createdAt DESC LIMIT :limit")
    @Transaction
    abstract suspend fun messagesForChannelEqualOrOlderThan(
        cid: String,
        limit: Int = 100,
        dateFilter: Date,
    ): List<MessageEntity>

    @Query("SELECT * from stream_chat_message WHERE cid = :cid ORDER BY createdAt DESC LIMIT :limit")
    @Transaction
    abstract suspend fun messagesForChannel(cid: String, limit: Int = 100): List<MessageEntity>

    @Query("DELETE from stream_chat_message WHERE cid = :cid AND createdAt < :deleteMessagesBefore")
    abstract suspend fun deleteChannelMessagesBefore(cid: String, deleteMessagesBefore: Date)

    @Query("DELETE from stream_chat_message WHERE cid = :cid AND id = :messageId")
    abstract suspend fun deleteMessage(cid: String, messageId: String)

    @Query("SELECT * FROM stream_chat_message WHERE stream_chat_message.id IN (:ids)")
    @Transaction
    abstract suspend fun select(ids: List<String>): List<MessageEntity>

    @Query("SELECT * FROM stream_chat_message WHERE stream_chat_message.id IN (:id)")
    @Transaction
    abstract suspend fun select(id: String): MessageEntity?

    @Query("SELECT * FROM stream_chat_message WHERE stream_chat_message.syncStatus IN (:syncStatus) ORDER BY createdAt ASC")
    @Transaction
    abstract suspend fun selectSyncNeeded(syncStatus: SyncStatus = SyncStatus.SYNC_NEEDED): List<MessageEntity>
}
