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

package io.getstream.chat.android.offline.repository.domain.channelconfig.internal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
internal interface ChannelConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun insert(channelConfigEntities: List<ChannelConfigEntity>) {
        insertConfigs(channelConfigEntities.map(ChannelConfigEntity::channelConfigInnerEntity))
        insertCommands(channelConfigEntities.flatMap(ChannelConfigEntity::commands))
    }

    @Transaction
    suspend fun insert(channelConfigEntity: ChannelConfigEntity) {
        insertConfig(channelConfigEntity.channelConfigInnerEntity)
        insertCommands(channelConfigEntity.commands)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(channelConfigInnerEntity: ChannelConfigInnerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(channelConfigInnerEntities: List<ChannelConfigInnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommands(commands: List<CommandInnerEntity>)

    @Transaction
    @Query("SELECT * FROM stream_chat_channel_config LIMIT 100")
    suspend fun selectAll(): List<ChannelConfigEntity>

    @Query("DELETE FROM $COMMAND_INNER_ENTITY_TABLE_NAME")
    suspend fun deleteCommands()

    @Query("DELETE FROM $CHANNEL_CONFIG_INNER_ENTITY_TABLE_NAME")
    suspend fun deleteConfigs()

    suspend fun deleteAll() {
        deleteConfigs()
        deleteCommands()
    }
}
