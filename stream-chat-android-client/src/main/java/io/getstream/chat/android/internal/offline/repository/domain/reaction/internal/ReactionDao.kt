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

package io.getstream.chat.android.internal.offline.repository.domain.reaction.internal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

@Dao
internal interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reactionEntity: ReactionEntity)

    @Query("SELECT * FROM $REACTION_ENTITY_TABLE_NAME WHERE id = :id")
    suspend fun selectReactionById(id: Int): ReactionEntity?

    @Query("SELECT * FROM $REACTION_ENTITY_TABLE_NAME WHERE id IN (:ids)")
    suspend fun selectReactionsByIds(ids: List<Int>): List<ReactionEntity>

    @Query(
        "SELECT id FROM $REACTION_ENTITY_TABLE_NAME " +
            "WHERE syncStatus = :syncStatus " +
            "ORDER BY syncStatus ASC " +
            "LIMIT :limit",
    )
    suspend fun selectIdsSyncStatus(syncStatus: SyncStatus, limit: Int = NO_LIMIT): List<Int>

    @Query(
        "SELECT * FROM stream_chat_reaction " +
            "WHERE syncStatus = :syncStatus " +
            "ORDER BY syncStatus ASC " +
            "LIMIT :limit",
    )
    suspend fun selectSyncStatus(syncStatus: SyncStatus, limit: Int = NO_LIMIT): List<ReactionEntity>

    @Query(
        "SELECT * FROM $REACTION_ENTITY_TABLE_NAME " +
            "WHERE stream_chat_reaction.type = :reactionType " +
            "AND stream_chat_reaction.messageid = :messageId " +
            "AND userId = :userId",
    )
    suspend fun selectUserReactionToMessage(reactionType: String, messageId: String, userId: String): ReactionEntity?

    @Query(
        "SELECT * FROM $REACTION_ENTITY_TABLE_NAME " +
            "WHERE stream_chat_reaction.messageid = :messageId " +
            "AND userId = :userId",
    )
    suspend fun selectUserReactionsToMessage(messageId: String, userId: String): List<ReactionEntity>

    @Query(
        "UPDATE $REACTION_ENTITY_TABLE_NAME " +
            "SET deletedAt = :deletedAt " +
            "WHERE userId = :userId " +
            "AND messageId = :messageId",
    )
    suspend fun setDeleteAt(userId: String, messageId: String, deletedAt: Date)

    @Delete
    suspend fun delete(reactionEntity: ReactionEntity)

    @Query("DELETE FROM $REACTION_ENTITY_TABLE_NAME")
    suspend fun deleteAll()

    private companion object {
        private const val NO_LIMIT: Int = -1
    }
}
