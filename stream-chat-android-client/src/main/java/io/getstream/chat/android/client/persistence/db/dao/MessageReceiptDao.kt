/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.persistence.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.client.persistence.db.entity.MessageReceiptEntity

@Dao
internal interface MessageReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(receipts: List<MessageReceiptEntity>)

    @Query("SELECT * FROM message_receipt ORDER BY createdAt ASC LIMIT :limit")
    suspend fun selectAll(limit: Int): List<MessageReceiptEntity>

    @Query("DELETE FROM message_receipt WHERE messageId IN (:messageIds)")
    suspend fun deleteByMessageIds(messageIds: List<String>)

    @Query("DELETE FROM message_receipt")
    suspend fun deleteAll()
}
