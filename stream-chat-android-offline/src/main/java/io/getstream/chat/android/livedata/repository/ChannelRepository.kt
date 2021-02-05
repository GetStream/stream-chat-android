package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.ChannelDao
import io.getstream.chat.android.livedata.repository.mapper.toEntity
import io.getstream.chat.android.livedata.repository.mapper.toModel
import java.util.Date

internal class ChannelRepository(
    private val channelDao: ChannelDao,
    private val getUser: suspend (userId: String) -> User,
    private val getMessage: suspend (messageId: String) -> Message?,
    cacheSize: Int = 100,
) {
    // the channel cache is simple, just keeps the last several users in memory
    private val channelCache = LruCache<String, Channel>(cacheSize)

    suspend fun insert(channel: Channel) {
        updateCache(listOf(channel))
        channelDao.insert(channel.toEntity())
    }

    private fun updateCache(channels: Collection<Channel>) {
        for (channel in channels) {
            channelCache.put(channel.cid, channel)
        }
    }

    suspend fun insertChannels(channels: Collection<Channel>) {
        if (channels.isEmpty()) return
        updateCache(channels)
        channelDao.insertMany(channels.map(Channel::toEntity))
    }

    suspend fun delete(cid: String) {
        channelCache.remove(cid)
        channelDao.delete(cid)
    }

    suspend fun select(cid: String): Channel? {
        return select(listOf(cid)).getOrNull(0)
    }

    suspend fun select(channelCIDs: List<String>): List<Channel> {
        val cachedChannels: MutableList<Channel> = channelCIDs.mapNotNullTo(mutableListOf(), channelCache::get)
        val missingChannelIds = channelCIDs.filter { channelCache.get(it) == null }
        val dbChannels = channelDao.select(missingChannelIds).map { it.toModel(getUser, getMessage) }.toMutableList()
        updateCache(dbChannels)
        dbChannels.addAll(cachedChannels)
        return dbChannels
    }

    internal suspend fun selectSyncNeeded(): List<Channel> {
        return channelDao.selectSyncNeeded().map { it.toModel(getUser, getMessage) }
    }

    internal suspend fun setDeletedAt(cid: String, deletedAt: Date) {
        channelCache.remove(cid)
        channelDao.setDeletedAt(cid, deletedAt)
    }

    internal suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date) {
        channelCache.remove(cid)
        channelDao.setHidden(cid, hidden, hideMessagesBefore)
    }

    internal suspend fun setHiddenForChannel(cid: String, hidden: Boolean) {
        channelCache.remove(cid)
        channelDao.setHidden(cid, hidden)
    }
}
