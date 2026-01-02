/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.message.internal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.ReplyAttachmentEntity

@Dao
internal interface ReplyMessageDao {

    @Query("SELECT * FROM $REPLY_MESSAGE_ENTITY_TABLE_NAME WHERE id = :id")
    @Transaction
    suspend fun selectById(id: String): ReplyMessageEntity?

    @Transaction
    suspend fun insert(replyMessageEntities: List<ReplyMessageEntity>) {
        insertAttachments(replyMessageEntities.flatMap(ReplyMessageEntity::attachments))
        insertInnerEntity(replyMessageEntities.map(ReplyMessageEntity::replyMessageInnerEntity))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInnerEntity(replyMessageEntities: List<ReplyMessageInnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachmentEntities: List<ReplyAttachmentEntity>)

    @Delete
    suspend fun delete(replyMessageInnerEntity: ReplyMessageInnerEntity)

    @Query("DELETE FROM $REPLY_MESSAGE_ENTITY_TABLE_NAME")
    suspend fun deleteAll()
}
