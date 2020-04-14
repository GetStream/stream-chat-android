package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.dao.ChannelDao
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.entity.ChannelEntity

class ChannelRepository(var channelDao: ChannelDao) {
    suspend fun insert(channel: Channel) {
        channelDao.insert(ChannelEntity(channel))
    }

    suspend fun insert(channelEntity: ChannelEntity) {

        channelDao.insert(channelEntity)
    }

    suspend fun insertChannelEntities(channelEntities: List<ChannelEntity>) {

        channelDao.insertMany(channelEntities)
    }

    suspend fun insert(channels: List<Channel>) {
        var entities = mutableListOf<ChannelEntity>()
        for (channel in channels) {
            entities.add(ChannelEntity(channel))
        }

        channelDao.insertMany(entities)
    }


    suspend fun select(cid: String): ChannelEntity? {
        return channelDao.select(cid)
    }
    suspend fun select(channelCIDs: List<String>): List<ChannelEntity> {
        return channelDao.select(channelCIDs)
    }

    suspend fun selectSyncNeeded(): List<ChannelEntity> {
        return channelDao.selectSyncNeeded()
    }
}