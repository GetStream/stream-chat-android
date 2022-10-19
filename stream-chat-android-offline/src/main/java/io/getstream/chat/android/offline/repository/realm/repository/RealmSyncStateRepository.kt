package io.getstream.chat.android.offline.repository.realm.repository

import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.android.offline.repository.realm.entity.SyncStateEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.toDomain
import io.getstream.chat.android.offline.repository.realm.entity.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query

internal class RealmSyncStateRepository(private val realm: Realm) : SyncStateRepository {

    override suspend fun clear() {

    }

    override suspend fun insertSyncState(syncState: SyncState) {
        realm.writeBlocking {
            copyToRealm(syncState.toRealm())
        }
    }

    override suspend fun selectSyncState(userId: String): SyncState? =
        realm.query<SyncStateEntityRealm>("user_id == '$userId'")
            .first()
            .find()
            ?.toDomain()
}
