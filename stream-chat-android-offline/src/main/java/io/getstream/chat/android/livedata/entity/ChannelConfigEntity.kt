package io.getstream.chat.android.livedata.entity

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
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val isTypingEvents: Boolean = false,
    val isReadEvents: Boolean = false,
    val isConnectEvents: Boolean = false,
    val isSearch: Boolean = false,
    val isReactionsEnabled: Boolean = false,
    val isRepliesEnabled: Boolean = false,
    val isMutes: Boolean = false,
    val maxMessageLength: Int = Int.MAX_VALUE,
    val automod: String = "",
    val infinite: String = "",
    val name: String = "",
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
    val channelType: String,
) {
    @PrimaryKey
    var id: Int = name.hashCode() + description.hashCode() + args.hashCode() + set.hashCode() + channelType.hashCode()
}

internal data class ChannelConfigEntity(
    @Embedded val channelConfigInnerEntity: ChannelConfigInnerEntity,
    @Relation(parentColumn = "channelType", entityColumn = "channelType", entity = CommandInnerEntity::class)
    val commands: List<CommandInnerEntity>,
)
