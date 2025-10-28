/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.repository.domain.message.attachment.internal

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity.Companion.ATTACHMENT_ENTITY_TABLE_NAME
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageInnerEntity

@Entity(
    tableName = ATTACHMENT_ENTITY_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = MessageInnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
    indices = [Index("messageId")],
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
    val name: String?,
    val fallback: String?,
    val uploadFilePath: String?,
    var originalHeight: Int?,
    var originalWidth: Int?,
    @Embedded
    var uploadState: UploadStateEntity? = null,
    val extraData: Map<String, Any>,
) {
    companion object {
        internal const val EXTRA_DATA_ID_KEY = "extra_data_id_key"
        internal const val ATTACHMENT_ENTITY_TABLE_NAME = "attachment_inner_entity"
        internal fun generateId(messageId: String, index: Int): String = messageId + "_$index"
    }
}

internal data class UploadStateEntity(val statusCode: Int, val errorMessage: String?) {
    internal companion object {
        internal const val UPLOAD_STATE_SUCCESS = 1
        internal const val UPLOAD_STATE_IN_PROGRESS = 2
        internal const val UPLOAD_STATE_FAILED = 3
    }
}
