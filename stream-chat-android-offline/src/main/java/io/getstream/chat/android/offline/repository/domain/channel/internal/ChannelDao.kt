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
 
package io.getstream.chat.android.offline.repository.domain.channel.internal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

@Dao
internal interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channelEntity: ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(channelEntities: List<ChannelEntity>)

    @Transaction
    @Query("SELECT cid FROM stream_chat_channel_state")
    suspend fun selectAllCids(): List<String>

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
            "WHERE stream_chat_channel_state.syncStatus IN (:syncStatus)"
    )
    suspend fun selectSyncNeeded(syncStatus: SyncStatus = SyncStatus.SYNC_NEEDED): List<ChannelEntity>

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
            "WHERE stream_chat_channel_state.cid IN (:cids)"
    )
    suspend fun select(cids: List<String>): List<ChannelEntity>

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
            "WHERE stream_chat_channel_state.cid IN (:cid)"
    )
    suspend fun select(cid: String?): ChannelEntity?

    @Query("DELETE from stream_chat_channel_state WHERE cid = :cid")
    suspend fun delete(cid: String)

    @Query("UPDATE stream_chat_channel_state SET deletedAt = :deletedAt WHERE cid = :cid")
    suspend fun setDeletedAt(cid: String, deletedAt: Date)

    @Query("UPDATE stream_chat_channel_state SET hidden = :hidden, hideMessagesBefore = :hideMessagesBefore WHERE cid = :cid")
    suspend fun setHidden(cid: String, hidden: Boolean, hideMessagesBefore: Date)

    @Query("UPDATE stream_chat_channel_state SET hidden = :hidden WHERE cid = :cid")
    suspend fun setHidden(cid: String, hidden: Boolean)
}
