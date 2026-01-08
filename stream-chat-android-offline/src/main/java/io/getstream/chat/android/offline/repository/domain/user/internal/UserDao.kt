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

package io.getstream.chat.android.offline.repository.domain.user.internal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userEntity: UserEntity)

    @Query("SELECT * FROM $USER_ENTITY_TABLE_NAME WHERE stream_chat_user.id IN (:ids)")
    suspend fun select(ids: List<String>): List<UserEntity>

    @Query("SELECT * FROM $USER_ENTITY_TABLE_NAME WHERE stream_chat_user.id IN (:id)")
    suspend fun select(id: String): UserEntity?

    @Query("SELECT * FROM $USER_ENTITY_TABLE_NAME ORDER BY name ASC LIMIT :limit OFFSET :offset")
    fun selectAllUser(limit: Int, offset: Int): List<UserEntity>

    @Query(
        "SELECT * FROM $USER_ENTITY_TABLE_NAME " +
            "WHERE name " +
            "LIKE :searchString " +
            "ORDER BY name " +
            "ASC LIMIT :limit " +
            "OFFSET :offset",
    )
    fun selectUsersLikeName(searchString: String, limit: Int, offset: Int): List<UserEntity>

    @Query("DELETE FROM $USER_ENTITY_TABLE_NAME")
    suspend fun deleteAll()
}
