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

package io.getstream.chat.android.offline.repository.domain.reaction.internal

import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import java.util.Date

/**
 * A [ReactionDao] implementation which lazily retrieves the original [ReactionDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverableReactionDao(private val getDatabase: () -> ChatDatabase) : ReactionDao {

    private val delegate: ReactionDao
        get() = getDatabase().reactionDao()

    override suspend fun insert(reactionEntity: ReactionEntity) {
        delegate.insert(reactionEntity)
    }

    override suspend fun selectReactionById(id: Int): ReactionEntity? {
        return delegate.selectReactionById(id)
    }

    override suspend fun selectReactionsByIds(ids: List<Int>): List<ReactionEntity> {
        return delegate.selectReactionsByIds(ids)
    }

    override suspend fun selectIdsSyncStatus(syncStatus: SyncStatus, limit: Int): List<Int> {
        return delegate.selectIdsSyncStatus(syncStatus, limit)
    }

    override suspend fun selectSyncStatus(syncStatus: SyncStatus, limit: Int): List<ReactionEntity> {
        return delegate.selectSyncStatus(syncStatus, limit)
    }

    override suspend fun selectUserReactionToMessage(
        reactionType: String,
        messageId: String,
        userId: String,
    ): ReactionEntity? {
        return delegate.selectUserReactionToMessage(reactionType, messageId, userId)
    }

    override suspend fun selectUserReactionsToMessage(messageId: String, userId: String): List<ReactionEntity> {
        return delegate.selectUserReactionsToMessage(messageId, userId)
    }

    override suspend fun setDeleteAt(userId: String, messageId: String, deletedAt: Date) {
        delegate.setDeleteAt(userId, messageId, deletedAt)
    }

    override suspend fun delete(reactionEntity: ReactionEntity) {
        delegate.delete(reactionEntity)
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
