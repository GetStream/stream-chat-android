package io.getstream.chat.android.offline.repository.domain.channel.internal

import androidx.annotation.VisibleForTesting
import androidx.collection.LruCache
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.toModel
import java.util.Date

internal interface ChannelRepository {
    suspend fun insertChannel(channel: Channel)
    suspend fun insertChannels(channels: Collection<Channel>)
    suspend fun deleteChannel(cid: String)
    suspend fun selectChannelWithoutMessages(cid: String): Channel?

    /**
     * Selects all channels' cids.
     *
     * @return A list of channels' cids stored in the repository.
     */
    suspend fun selectAllCids(): List<String>

    /**
     * Select channels by full channel IDs [Channel.cid]
     *
     * @param channelCIDs A list of [Channel.cid] as query specification.
     * @param forceCache A boolean flag that forces cache in repository and fetches data directly in database if passed
     * value is true.
     *
     * @return A list of channels found in repository.
     */
    suspend fun selectChannels(channelCIDs: List<String>, forceCache: Boolean = false): List<Channel>
    suspend fun selectChannelsSyncNeeded(): List<Channel>
    suspend fun setChannelDeletedAt(cid: String, deletedAt: Date)
    suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date)
    suspend fun setHiddenForChannel(cid: String, hidden: Boolean)
    suspend fun selectMembersForChannel(cid: String): List<Member>
    suspend fun updateMembersForChannel(cid: String, members: List<Member>)
    suspend fun evictChannel(cid: String)

    @VisibleForTesting
    fun clearChannelCache()
}

internal class ChannelRepositoryImpl(
    private val channelDao: ChannelDao,
    private val getUser: suspend (userId: String) -> User,
    private val getMessage: suspend (messageId: String) -> Message?,
    cacheSize: Int = 100,
) : ChannelRepository {
    // the channel cache is simple, just keeps the last several users in memory
    private val channelCache = LruCache<String, Channel>(cacheSize)

    override suspend fun insertChannel(channel: Channel) {
        updateCache(listOf(channel))
        channelDao.insert(channel.toEntity())
    }

    override suspend fun insertChannels(channels: Collection<Channel>) {
        if (channels.isEmpty()) return
        updateCache(channels)
        channelDao.insertMany(channels.map(Channel::toEntity))
    }

    override suspend fun deleteChannel(cid: String) {
        channelCache.remove(cid)
        channelDao.delete(cid)
    }

    override suspend fun selectChannelWithoutMessages(cid: String): Channel? {
        return selectChannels(listOf(cid)).getOrNull(0)
    }

    override suspend fun selectAllCids(): List<String> = channelDao.selectAllCids()

    override suspend fun selectChannels(channelCIDs: List<String>, forceCache: Boolean): List<Channel> {
        if (channelCIDs.isEmpty()) {
            return emptyList()
        }
        return if (forceCache) {
            fetchChannels(channelCIDs)
        } else {
            val cachedChannels: MutableList<Channel> = channelCIDs.mapNotNullTo(mutableListOf(), channelCache::get)
            val missingChannelIds = channelCIDs.filter { channelCache.get(it) == null }
            val dbChannels = fetchChannels(missingChannelIds).toMutableList()
            dbChannels.addAll(cachedChannels)
            dbChannels
        }
    }

    private suspend fun fetchChannels(channelCIDs: List<String>): List<Channel> {
        return channelDao.select(channelCIDs).map { it.toModel(getUser, getMessage) }.also(::updateCache)
    }

    override suspend fun selectChannelsSyncNeeded(): List<Channel> {
        return channelDao.selectSyncNeeded().map { it.toModel(getUser, getMessage) }
    }

    override suspend fun setChannelDeletedAt(cid: String, deletedAt: Date) {
        channelCache.remove(cid)
        channelDao.setDeletedAt(cid, deletedAt)
    }

    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date) {
        channelCache.remove(cid)
        channelDao.setHidden(cid, hidden, hideMessagesBefore)
    }

    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean) {
        channelCache.remove(cid)
        channelDao.setHidden(cid, hidden)
    }

    // Allows us to avoid enriching channel just to select members
    override suspend fun selectMembersForChannel(cid: String): List<Member> {
        return channelDao.select(cid)?.members?.values?.map { it.toModel(getUser) } ?: emptyList()
    }

    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) {
        members
            .map { it.toEntity() }
            .associateBy { it.userId }
            .let { memberMap ->
                channelDao.select(cid)?.copy(members = memberMap)
            }
            ?.let { updatedChannel ->
                channelDao.insert(updatedChannel)
            }
    }

    private fun updateCache(channels: Collection<Channel>) {
        for (channel in channels) {
            channelCache.put(channel.cid, channel)
        }
    }

    override suspend fun evictChannel(cid: String) {
        channelCache.remove(cid)
    }

    override fun clearChannelCache() {
        channelCache.evictAll()
    }
}
