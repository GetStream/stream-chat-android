package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.Config

@Entity(tableName = "stream_chat_channel_config")
internal data class ChannelConfigEntity(@PrimaryKey var channelType: String, var config: Config)
