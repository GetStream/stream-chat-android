package io.getstream.chat.android.offline.repository.domain.message.attachment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
internal interface AttachmentToUploadDao {

    @Query("SELECT * FROM attachment_to_upload")
    suspend fun getAttachmentsToUpload(): List<AttachmentToUploadEntity>

    @Query("SELECT * FROM attachment_to_upload where messageId == :messageId")
    suspend fun getAttachmentsToUploadForMessage(messageId: Int): List<AttachmentToUploadEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttachmentToUpload(attachmentToUploadEntities: List<AttachmentToUploadEntity>)

    @Update
    suspend fun updateAttachmentsToUpload(attachmentToUploadEntities: List<AttachmentToUploadEntity>)

    @Delete
    suspend fun deleteAttachmentsToUpload(attachmentToUploadEntities: List<AttachmentToUploadEntity>)
}
