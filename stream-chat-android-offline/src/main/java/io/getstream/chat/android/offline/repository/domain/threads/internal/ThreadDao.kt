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

package io.getstream.chat.android.offline.repository.domain.threads.internal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO for accessing threads data.
 */
@Dao
internal interface ThreadDao {

    /**
     * Inserts the given Thread into the DB.
     *
     * @param thread The [ThreadEntity] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: ThreadEntity)

    /**
     * Inserts the given list of Threads into the DB.
     *
     * @param threads The list of [ThreadEntity]s to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreads(threads: List<ThreadEntity>)

    /**
     * Retrieves a [ThreadEntity] by its ID.
     *
     * @param id The ID of the [ThreadEntity] to retrieve.
     */
    @Query("SELECT * FROM $THREAD_ENTITY_TABLE_NAME WHERE parentMessageId=:id")
    suspend fun selectThread(id: String): ThreadEntity?

    /**
     * Retrieves a list of [ThreadEntity] by their IDs.
     *
     * @param ids The IDs of the [ThreadEntity]s to retrieve.
     */
    @Query("SELECT * FROM $THREAD_ENTITY_TABLE_NAME WHERE parentMessageId in (:ids)")
    suspend fun selectThreads(ids: Collection<String>): List<ThreadEntity>

    /**
     * Deletes all threads from the given channel.
     *
     * @param cid The ID of the Channel to delete the threads from.
     */
    @Query("DELETE from $THREAD_ENTITY_TABLE_NAME WHERE cid=:cid")
    suspend fun deleteThreads(cid: String)

    /**
     * Deletes all data from the threads table.
     */
    @Query("DELETE from $THREAD_ENTITY_TABLE_NAME")
    suspend fun deleteAll()
}
