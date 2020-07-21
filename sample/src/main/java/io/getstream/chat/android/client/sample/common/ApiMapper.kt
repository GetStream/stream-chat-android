package io.getstream.chat.android.client.sample.common

object ApiMapper {

    fun mapChannel(channel: ChatChannel): Channel {
        return Channel().apply {
            remoteId = channel.id
            name = channel.name
            updatedAt = channel.updatedAt
        }
    }

    fun mapChannels(channels: List<ChatChannel>): List<Channel> {
        return channels.map {
            mapChannel(it)
        }
    }
}
