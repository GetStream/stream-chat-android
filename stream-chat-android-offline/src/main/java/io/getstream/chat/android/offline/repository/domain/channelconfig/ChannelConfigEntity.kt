package io.getstream.chat.android.offline.repository.domain.channelconfig

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

@Entity(tableName = "stream_chat_channel_config")
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
    val isRepliesEnabled: Boolean,
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
)

@Entity(
    tableName = "command_inner_entity",
    foreignKeys = [
        ForeignKey(
            entity = ChannelConfigInnerEntity::class,
            parentColumns = ["channelType"],
            childColumns = ["channelType"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
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
