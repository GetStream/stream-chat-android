package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.livedata.dao.SyncStateDao
import io.getstream.chat.android.livedata.entity.SyncStateEntity

internal class SyncStateRepository(var syncStateDao: SyncStateDao) {
    suspend fun insert(syncStateEntity: SyncStateEntity) {
        syncStateDao.insert(syncStateEntity)
    }

    suspend fun select(userId: String): SyncStateEntity? {
        return syncStateDao.select(userId)
    }
}
