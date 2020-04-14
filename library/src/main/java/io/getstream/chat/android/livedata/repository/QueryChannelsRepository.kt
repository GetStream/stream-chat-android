package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.livedata.dao.QueryChannelsDao
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity

class QueryChannelsRepository(var queryChannelsDao: QueryChannelsDao) {
    suspend fun insert(queryChannelsEntity: QueryChannelsEntity) {
        queryChannelsDao.insert(queryChannelsEntity)
    }
    suspend fun selectQuery(id: String): QueryChannelsEntity? {
        return queryChannelsDao.select(id)
    }
}