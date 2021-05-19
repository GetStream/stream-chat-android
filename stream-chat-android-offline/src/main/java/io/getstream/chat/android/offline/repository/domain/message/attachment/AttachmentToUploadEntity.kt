package io.getstream.chat.android.offline.repository.domain.message.attachment

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.utils.SyncStatus

@Entity(tableName = "attachment_to_upload")
internal data class AttachmentToUploadEntity(
    @PrimaryKey
    var uploadId: String,
    @ColumnInfo(index = true)
    val messageId: String,
    val authorName: String?,
    val titleLink: String?,
    val thumbUrl: String?,
    val imageUrl: String?,
    val assetUrl: String?,
    val ogUrl: String?,
    val mimeType: String?,
    val fileSize: Int,
    val title: String?,
    val text: String?,
    val type: String?,
    val image: String?,
    val url: String?,
    val name: String?,
    val fallback: String?,
    val uploadFilePath: String?,
    @Embedded val uploadState: UploadStateEntity?,
    val extraData: Map<String, Any>,
    val syncStatus: SyncStatus = SyncStatus.SYNC_NEEDED,
)
