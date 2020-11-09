package io.getstream.chat.android.livedata.repository

import androidx.collection.LruCache
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.dao.ChannelDao
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.extensions.isPermanent

internal class ChannelRepository(
    var channelDao: ChannelDao,
    var cacheSize: Int = 100,
    var currentUser: User,
    var client: ChatClient
) {
    // the channel cache is simple, just keeps the last 100 users in memory
    var channelCache = LruCache<String, ChannelEntity>(cacheSize)

    suspend fun insertChannel(channel: Channel) {
        val channelEntity = ChannelEntity(channel)
        insert(listOf(channelEntity))
    }

    suspend fun insertChannel(channels: List<Channel>) {
        val entities = channels.map { ChannelEntity(it) }
        insert(entities)
    }

    suspend fun insert(channelEntity: ChannelEntity) {
        insert(listOf(channelEntity))
    }

    private fun updateCache(channelEntities: List<ChannelEntity>) {
        for (channelEntity in channelEntities) {
            channelCache.put(channelEntity.cid, channelEntity)
        }
    }

    suspend fun insertChannels(channels: Collection<Channel>) {
        insert(channels.map(::ChannelEntity))
    }

    suspend fun insert(channelEntities: List<ChannelEntity>) {
        if (channelEntities.isEmpty()) return
        channelDao.insertMany(channelEntities)
        updateCache(channelEntities)
    }

    suspend fun delete(cid: String) {
        channelCache.remove(cid)
        channelDao.delete(cid)
    }

    suspend fun select(cid: String): ChannelEntity? {
        return select(listOf(cid)).getOrElse(0) { null }
    }

    suspend fun select(channelCIDs: List<String>): List<ChannelEntity> {
        val cachedChannels: MutableList<ChannelEntity> = mutableListOf()
        for (cid in channelCIDs) {
            val channelEntity = channelCache.get(cid)
            channelEntity?.let { cachedChannels.add(it) }
        }
        val missingChannelIds = channelCIDs.filter { channelCache.get(it) == null }
        val dbChannels = channelDao.select(missingChannelIds).toMutableList()
        updateCache(dbChannels)
        dbChannels.addAll(cachedChannels)
        return dbChannels
    }

    suspend fun selectAllForCurrentUser(): List<ChannelEntity> =
        channelDao.selectAll().filter { it.members.keys.contains(currentUser.id) }

    suspend fun selectSyncNeeded(): List<ChannelEntity> {
        return channelDao.selectSyncNeeded()
    }

    suspend fun retryChannels(): List<ChannelEntity> {
        val channelEntities = selectSyncNeeded()

        for (channelEntity in channelEntities) {
            // TODO: what about channel.members
            val result =
                client.createChannel(channelEntity.type, channelEntity.channelId, channelEntity.extraData).execute()
            if (result.isSuccess) {
                channelEntity.syncStatus = SyncStatus.COMPLETED
                insert(channelEntity)
            } else if (result.isError && result.error().isPermanent()) {
                channelEntity.syncStatus = SyncStatus.FAILED_PERMANENTLY
                insert(channelEntity)
            }
        }
        return channelEntities
    }
}
