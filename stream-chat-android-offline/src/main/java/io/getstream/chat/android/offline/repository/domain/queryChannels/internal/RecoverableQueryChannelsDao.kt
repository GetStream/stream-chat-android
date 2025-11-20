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

package io.getstream.chat.android.offline.repository.domain.queryChannels.internal

import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase

/**
 * A [QueryChannelsDao] implementation which lazily retrieves the original [QueryChannelsDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverableQueryChannelsDao(private val getDatabase: () -> ChatDatabase) : QueryChannelsDao {

    private val delegate: QueryChannelsDao
        get() = getDatabase().queryChannelsDao()

    override suspend fun insert(queryChannelsEntity: QueryChannelsEntity) {
        delegate.insert(queryChannelsEntity)
    }

    override suspend fun select(id: String): QueryChannelsEntity? {
        return delegate.select(id)
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
