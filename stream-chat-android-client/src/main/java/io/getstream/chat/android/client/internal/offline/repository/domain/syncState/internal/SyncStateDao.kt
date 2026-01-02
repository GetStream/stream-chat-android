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

package io.getstream.chat.android.client.internal.offline.repository.domain.syncState.internal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface SyncStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncStateEntity: SyncStateEntity)

    @Query(
        "SELECT * FROM $SYNC_STATE_ENTITY_TABLE_NAME " +
            "WHERE stream_sync_state.userId = :userId",
    )
    suspend fun select(userId: String): SyncStateEntity?

    @Query("DELETE FROM $SYNC_STATE_ENTITY_TABLE_NAME")
    suspend fun deleteAll()
}
