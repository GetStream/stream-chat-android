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

package io.getstream.chat.android.client.internal.offline.repository.domain.scheduled.internal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface ScheduledMessageDao {

    @Query("SELECT * FROM $SCHEDULED_MESSAGE_ENTITY_TABLE_NAME")
    suspend fun selectAll(): List<ScheduledMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScheduledMessageEntity)

    @Query("DELETE FROM $SCHEDULED_MESSAGE_ENTITY_TABLE_NAME WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM $SCHEDULED_MESSAGE_ENTITY_TABLE_NAME")
    suspend fun deleteAll()
}
