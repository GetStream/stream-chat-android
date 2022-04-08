package io.getstream.chat.android.client.persistence.repository.inmemory

import io.getstream.chat.android.client.models.SyncState
import io.getstream.chat.android.client.persistence.repository.SyncStateRepository

internal class SyncStateInMemoryRepository: SyncStateRepository {

    override suspend fun insertSyncState(syncState: SyncState) {
        TODO("Not yet implemented")
    }

    override suspend fun selectSyncState(userId: String): SyncState? {
        TODO("Not yet implemented")
    }
}
