package io.getstream.chat.android.offline.repository.domain.message.attachment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AttachmentToUploadDao {

    @Query("SELECT * FROM attachment_to_upload")
    suspend fun getAttachmentsToUpload(): List<AttachmentToUploadEntity>

    @Query("SELECT * FROM attachment_to_upload where messageId == :messageId")
    suspend fun getAttachmentsToUploadForMessage(messageId: String): List<AttachmentToUploadEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttachmentToUpload(vararg attachmentToUploadEntity: AttachmentToUploadEntity)

    @Update
    suspend fun updateAttachmentsToUpload(vararg attachmentToUploadEntity: AttachmentToUploadEntity)

    @Delete
    suspend fun deleteAttachmentsToUpload(vararg attachmentToUploadEntity: AttachmentToUploadEntity)

    @Query("SELECT * FROM attachment_to_upload")
    fun observeAttachmentsToUpload(): Flow<List<AttachmentToUploadEntity>>
}
