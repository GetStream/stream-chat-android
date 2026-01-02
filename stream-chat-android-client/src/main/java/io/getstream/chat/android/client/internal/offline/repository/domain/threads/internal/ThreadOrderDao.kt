/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.offline.repository.domain.threads.internal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO for accessing thread order data.
 */
@Dao
internal interface ThreadOrderDao {

    /**
     * Inserts the current order of the loaded threads.
     *
     * @param order The [ThreadOrderEntity] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreadOrder(order: ThreadOrderEntity)

    /**
     * Retrieves the order of the last loaded threads.
     *
     * @param id The ID of the [ThreadOrderEntity] to retrieve.
     */
    @Query("SELECT * FROM $THREAD_ORDER_ENTITY_TABLE_NAME WHERE id=:id")
    suspend fun selectThreadOrder(id: String): ThreadOrderEntity?

    /**
     * Deletes all data from the thread order table.
     */
    @Query("DELETE from $THREAD_ORDER_ENTITY_TABLE_NAME")
    suspend fun deleteAll()
}
