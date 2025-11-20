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

package io.getstream.chat.android.offline.repository.domain.syncState.internal

import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase

/**
 * A [SyncStateDao] implementation which lazily retrieves the original [SyncStateDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverableSyncStateDao(private val getDatabase: () -> ChatDatabase) : SyncStateDao {

    private val delegate: SyncStateDao
        get() = getDatabase().syncStateDao()

    override suspend fun insert(syncStateEntity: SyncStateEntity) {
        delegate.insert(syncStateEntity)
    }

    override suspend fun select(userId: String): SyncStateEntity? {
        return delegate.select(userId)
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
