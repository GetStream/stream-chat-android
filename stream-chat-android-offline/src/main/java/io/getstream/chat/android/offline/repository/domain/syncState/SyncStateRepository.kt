package io.getstream.chat.android.offline.repository.domain.syncState

import io.getstream.chat.android.offline.model.SyncState

internal interface SyncStateRepository {
    suspend fun insertSyncState(syncState: SyncState)
    suspend fun selectSyncState(userId: String): SyncState?
}

internal class SyncStateRepositoryImpl(private val syncStateDao: SyncStateDao) : SyncStateRepository {
    override suspend fun insertSyncState(syncState: SyncState) {
        syncStateDao.insert(syncState.toEntity())
    }

    override suspend fun selectSyncState(userId: String): SyncState? {
        return syncStateDao.select(userId)?.toModel()
    }
}
