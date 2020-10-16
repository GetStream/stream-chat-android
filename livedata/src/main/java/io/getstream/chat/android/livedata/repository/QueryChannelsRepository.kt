package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.livedata.dao.QueryChannelsDao
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity

internal class QueryChannelsRepository(var queryChannelsDao: QueryChannelsDao) {
    suspend fun insert(queryChannelsEntity: QueryChannelsEntity) {
        queryChannelsDao.insert(queryChannelsEntity)
    }
    suspend fun select(id: String): QueryChannelsEntity? {
        return queryChannelsDao.select(id)
    }
    suspend fun select(ids: List<String>): List<QueryChannelsEntity> {
        return queryChannelsDao.select(ids)
    }
}
