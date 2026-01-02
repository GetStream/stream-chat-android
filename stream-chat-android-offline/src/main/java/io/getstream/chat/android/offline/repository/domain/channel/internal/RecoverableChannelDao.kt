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

package io.getstream.chat.android.offline.repository.domain.channel.internal

import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import java.util.Date

/**
 * A [ChannelDao] implementation which lazily retrieves the original [ChannelDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverableChannelDao(private val getDatabase: () -> ChatDatabase) : ChannelDao {

    private val delegate: ChannelDao
        get() = getDatabase().channelStateDao()

    override suspend fun insert(channelEntity: ChannelEntity) {
        delegate.insert(channelEntity)
    }

    override suspend fun insertMany(channelEntities: List<ChannelEntity>) {
        delegate.insertMany(channelEntities)
    }

    override suspend fun selectAllCids(): List<String> {
        return delegate.selectAllCids()
    }

    override suspend fun selectCidsBySyncNeeded(syncStatus: SyncStatus, limit: Int): List<String> {
        return delegate.selectCidsBySyncNeeded(syncStatus, limit)
    }

    override suspend fun selectSyncNeeded(syncStatus: SyncStatus, limit: Int): List<ChannelEntity> {
        return delegate.selectSyncNeeded(syncStatus, limit)
    }

    override suspend fun select(cids: List<String>): List<ChannelEntity> {
        return delegate.select(cids)
    }

    override suspend fun select(cid: String?): ChannelEntity? {
        return delegate.select(cid)
    }

    override suspend fun delete(cid: String) {
        delegate.delete(cid)
    }

    override suspend fun setDeletedAt(cid: String, deletedAt: Date) {
        delegate.setDeletedAt(cid, deletedAt)
    }

    override suspend fun setHidden(cid: String, hidden: Boolean, hideMessagesBefore: Date) {
        delegate.setHidden(cid, hidden, hideMessagesBefore)
    }

    override suspend fun setHidden(cid: String, hidden: Boolean) {
        delegate.setHidden(cid, hidden)
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
