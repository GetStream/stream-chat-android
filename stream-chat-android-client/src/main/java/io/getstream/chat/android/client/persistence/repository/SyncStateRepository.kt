package io.getstream.chat.android.client.persistence.repository

import io.getstream.chat.android.client.models.SyncState

public interface SyncStateRepository {
    public suspend fun insertSyncState(syncState: SyncState)
    public suspend fun selectSyncState(userId: String): SyncState?
}
