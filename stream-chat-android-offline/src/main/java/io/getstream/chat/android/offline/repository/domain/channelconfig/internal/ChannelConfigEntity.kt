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

package io.getstream.chat.android.offline.repository.domain.channelconfig.internal

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

@Entity(tableName = CHANNEL_CONFIG_INNER_ENTITY_TABLE_NAME)
internal data class ChannelConfigInnerEntity(
    @PrimaryKey
    val channelType: String,
    val createdAt: Date?,
    val updatedAt: Date?,
    val name: String,
    val isTypingEvents: Boolean,
    val isReadEvents: Boolean,
    val isConnectEvents: Boolean,
    val isSearch: Boolean,
    val isReactionsEnabled: Boolean,
    val isThreadEnabled: Boolean,
    val isMutes: Boolean,
    val uploadsEnabled: Boolean,
    val urlEnrichmentEnabled: Boolean,
    val customEventsEnabled: Boolean,
    val pushNotificationsEnabled: Boolean,
    val messageRetention: String,
    val maxMessageLength: Int,
    val automod: String,
    val automodBehavior: String,
    val blocklistBehavior: String,
    val markMessagesPending: Boolean,
)

@Entity(
    tableName = COMMAND_INNER_ENTITY_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = ChannelConfigInnerEntity::class,
            parentColumns = ["channelType"],
            childColumns = ["channelType"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
internal data class CommandInnerEntity(
    val name: String,
    val description: String,
    val args: String,
    val set: String,
    @ColumnInfo(index = true)
    val channelType: String,
) {
    @PrimaryKey
    var id: Int = hashCode()
}

internal data class ChannelConfigEntity(
    @Embedded val channelConfigInnerEntity: ChannelConfigInnerEntity,
    @Relation(parentColumn = "channelType", entityColumn = "channelType", entity = CommandInnerEntity::class)
    val commands: List<CommandInnerEntity>,
)

internal const val COMMAND_INNER_ENTITY_TABLE_NAME = "command_inner_entity"
internal const val CHANNEL_CONFIG_INNER_ENTITY_TABLE_NAME = "stream_chat_channel_config"
