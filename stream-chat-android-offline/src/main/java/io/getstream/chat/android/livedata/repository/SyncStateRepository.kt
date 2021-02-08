package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.livedata.dao.SyncStateDao
import io.getstream.chat.android.livedata.model.SyncState
import io.getstream.chat.android.livedata.repository.mapper.toEntity
import io.getstream.chat.android.livedata.repository.mapper.toModel

internal interface SyncStateRepository {
    suspend fun insert(syncState: SyncState)
    suspend fun select(userId: String): SyncState?
}

internal class SyncStateRepositoryImpl(private val syncStateDao: SyncStateDao) : SyncStateRepository {
    override suspend fun insert(syncState: SyncState) {
        syncStateDao.insert(syncState.toEntity())
    }

    override suspend fun select(userId: String): SyncState? {
        return syncStateDao.select(userId)?.toModel()
    }
}
