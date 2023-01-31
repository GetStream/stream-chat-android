/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.docs.java.ui.guides.realm.repository

import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.docs.java.ui.guides.realm.entities.SyncStateEntityRealm
import io.getstream.chat.docs.java.ui.guides.realm.entities.toDomain
import io.getstream.chat.docs.java.ui.guides.realm.entities.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query

internal class RealmSyncStateRepository(private val realm: Realm) : SyncStateRepository {

    override suspend fun clear() {
        val syncStates = realm.query<SyncStateEntityRealm>().find()

        realm.write {
            delete(syncStates)
        }
    }

    override suspend fun insertSyncState(syncState: SyncState) {
        realm.writeBlocking {
            copyToRealm(syncState.toRealm(), UpdatePolicy.ALL)
        }
    }

    override suspend fun selectSyncState(userId: String): SyncState? =
        realm.query<SyncStateEntityRealm>("user_id == '$userId'")
            .first()
            .find()
            ?.toDomain()
}
