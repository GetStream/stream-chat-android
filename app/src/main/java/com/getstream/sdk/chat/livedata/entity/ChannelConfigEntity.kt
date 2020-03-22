package com.getstream.sdk.chat.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.models.Config

@Entity(tableName = "stream_chat_channel_config")
data class ChannelConfigEntity(@PrimaryKey var channelType: String) {

    lateinit var config: Config
}