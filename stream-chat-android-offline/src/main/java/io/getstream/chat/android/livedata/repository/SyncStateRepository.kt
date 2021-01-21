package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.livedata.dao.SyncStateDao
import io.getstream.chat.android.livedata.model.SyncState
import io.getstream.chat.android.livedata.repository.mapper.toEntity
import io.getstream.chat.android.livedata.repository.mapper.toModel

internal class SyncStateRepository(var syncStateDao: SyncStateDao) {
    suspend fun insert(syncState: SyncState) {
        syncStateDao.insert(syncState.toEntity())
    }

    suspend fun select(userId: String): SyncState? {
        return syncStateDao.select(userId)?.toModel()
    }
}
