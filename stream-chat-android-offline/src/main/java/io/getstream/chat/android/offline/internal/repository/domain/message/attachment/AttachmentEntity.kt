package io.getstream.chat.android.offline.internal.repository.domain.message.attachment

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.offline.internal.repository.domain.message.MessageInnerEntity

@Entity(
    tableName = "attachment_inner_entity",
    foreignKeys = [
        ForeignKey(
            entity = MessageInnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ],
    indices = [Index("messageId")]
)
internal data class AttachmentEntity(
    @ColumnInfo(index = true)
    @PrimaryKey
    val id: String,
    val messageId: String,
    val authorName: String?,
    val titleLink: String?,
    val authorLink: String?,
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
    @Embedded
    var uploadState: UploadStateEntity? = null,
    val extraData: Map<String, Any>,
) {
    companion object {
        internal const val EXTRA_DATA_ID_KEY = "extra_data_id_key"
        internal fun generateId(messageId: String, index: Int): String {
            return messageId + "_$index"
        }
    }
}

internal data class UploadStateEntity(val statusCode: Int, val errorMessage: String?) {
    internal companion object {
        internal const val UPLOAD_STATE_SUCCESS = 1
        internal const val UPLOAD_STATE_IN_PROGRESS = 2
        internal const val UPLOAD_STATE_FAILED = 3
    }
}
