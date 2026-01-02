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

package io.getstream.chat.android.client.internal.offline.repository.domain.syncState.internal

import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.sync.SyncState

/**
 * Repository to read and write data about the sync state of the SDK. This implementation uses database
 */
internal class DatabaseSyncStateRepository(private val syncStateDao: SyncStateDao) : SyncStateRepository {

    /**
     * Inserts a sync state.
     *
     * @param syncState [SyncState]
     */
    override suspend fun insertSyncState(syncState: SyncState) {
        syncStateDao.insert(syncState.toEntity())
    }

    /**
     * Selects a sync state.
     *
     * @param userId String
     */
    override suspend fun selectSyncState(userId: String): SyncState? {
        return syncStateDao.select(userId)?.toModel()
    }

    override suspend fun clear() {
        syncStateDao.deleteAll()
    }
}
