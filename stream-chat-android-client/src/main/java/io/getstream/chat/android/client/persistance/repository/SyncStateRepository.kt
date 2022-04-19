package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.sync.SyncState

public interface SyncStateRepository {
    public suspend fun insertSyncState(syncState: SyncState)
    public suspend fun selectSyncState(userId: String): SyncState?
}
