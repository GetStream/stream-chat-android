package io.getstream.chat.android.client.persistence.repository.inmemory

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.persistence.repository.ChannelRepository
import java.util.Date

internal class ChannelInMemoryRepository : ChannelRepository {

    override suspend fun insertChannel(channel: Channel) {
        TODO("Not yet implemented")
    }

    override suspend fun insertChannels(channels: Collection<Channel>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteChannel(cid: String) {
        TODO("Not yet implemented")
    }

    override suspend fun selectChannelWithoutMessages(cid: String): Channel? {
        TODO("Not yet implemented")
    }

    override suspend fun selectAllCids(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun selectChannels(channelCIDs: List<String>, forceCache: Boolean): List<Channel> {
        TODO("Not yet implemented")
    }

    override suspend fun selectChannelsSyncNeeded(): List<Channel> {
        TODO("Not yet implemented")
    }

    override suspend fun setChannelDeletedAt(cid: String, deletedAt: Date) {
        TODO("Not yet implemented")
    }

    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date) {
        TODO("Not yet implemented")
    }

    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun selectMembersForChannel(cid: String): List<Member> {
        TODO("Not yet implemented")
    }

    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) {
        TODO("Not yet implemented")
    }

    override suspend fun evictChannel(cid: String) {
        TODO("Not yet implemented")
    }

    override fun clearChannelCache() {
        TODO("Not yet implemented")
    }
}
