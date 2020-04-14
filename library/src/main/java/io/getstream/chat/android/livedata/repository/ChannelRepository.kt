package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.dao.ChannelDao
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.UserEntity

class ChannelRepository(var channelDao: ChannelDao, var cacheSize: Int = 100) {
    // the channel cache is simple, just keeps the last 100 users in memory
    var channelCache = LruCache<String, ChannelEntity>(cacheSize)

    suspend fun insert(channel: Channel) {
        val channelEntity = ChannelEntity(channel)
        insertChannelEntities(listOf(channelEntity))
    }

    suspend fun insert(channelEntity: ChannelEntity) {
        insertChannelEntities(listOf(channelEntity))
    }

    suspend fun insertChannelEntities(channelEntities: List<ChannelEntity>) {
        if (channelEntities.isEmpty()) return
        channelDao.insertMany(channelEntities)
        for (channelEntity in channelEntities) {
            channelCache.put(channelEntity.cid, channelEntity)
        }
    }

    suspend fun insert(channels: List<Channel>) {
        var entities = channels.map { ChannelEntity(it) }
        insertChannelEntities(entities)
    }

    suspend fun select(cid: String): ChannelEntity? {
        return select(listOf(cid)).getOrElse(0) {null}
    }

    suspend fun select(channelCIDs: List<String>): List<ChannelEntity> {
        val cachedChannels: MutableList<ChannelEntity> = mutableListOf()
        for (cid in channelCIDs) {
            val channelEntity = channelCache.get(cid)
            channelEntity?.let { cachedChannels.add(it) }
        }
        val missingChannelIds = channelCIDs.filter { channelCache.get(it) == null }
        val dbChannels = channelDao.select(missingChannelIds).toMutableList()
        for (channel in dbChannels) {
            channelCache.put(channel.cid, channel)
        }
        dbChannels.addAll(cachedChannels)
        return dbChannels
    }


    suspend fun selectSyncNeeded(): List<ChannelEntity> {
        return channelDao.selectSyncNeeded()
    }
}