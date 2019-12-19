package io.getstream.chat.android.core.poc.app.common

object ApiMapper {

    fun mapChannel(channel: io.getstream.chat.android.core.poc.library.Channel): Channel {
        return Channel().apply {
            remoteId = channel.id
            name = channel.name
            updatedAt = channel.updatedAt
        }
    }

    fun mapChannels(channels: List<io.getstream.chat.android.core.poc.library.Channel>): List<Channel> {
        return channels.map {
            mapChannel(it)
        }
    }
}