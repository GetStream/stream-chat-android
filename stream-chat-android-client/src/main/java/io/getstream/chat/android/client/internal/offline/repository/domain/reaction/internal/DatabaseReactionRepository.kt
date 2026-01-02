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

package io.getstream.chat.android.client.internal.offline.repository.domain.reaction.internal

import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * Repository to read and write reactions. This implementation uses database.
 * We don't do any caching on reactions since usage is infrequent.
 */
internal class DatabaseReactionRepository(
    private val reactionDao: ReactionDao,
    private val getUser: suspend (userId: String) -> User,
) : ReactionRepository {

    /**
     * Inserts a reaction.
     *
     * @param reaction [Reaction]
     */
    override suspend fun insertReaction(reaction: Reaction) {
        require(reaction.messageId.isNotEmpty()) { "message id can't be empty when creating a reaction" }
        require(reaction.type.isNotEmpty()) { "type can't be empty when creating a reaction" }
        require(reaction.userId.isNotEmpty()) { "user id can't be empty when creating a reaction" }

        reactionDao.insert(reaction.toEntity())
    }

    /**
     * Updates the Reaction.deletedAt for reactions of a message.
     *
     * @param userId String.
     * @param messageId String.
     * @param deletedAt Date.
     */
    override suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) {
        reactionDao.setDeleteAt(userId, messageId, deletedAt)
    }

    /**
     * Selects reaction with specified [id].
     *
     * @param id A [ReactionEntity.id] to search for.
     */
    override suspend fun selectReactionById(id: Int): Reaction? {
        return reactionDao.selectReactionById(id)?.toModel(getUser)
    }

    /**
     * Selects all reactions with specified [ids]
     *
     * @param ids A list of [ReactionEntity.id] to search for.
     */
    override suspend fun selectReactionsByIds(ids: List<Int>): List<Reaction> {
        return reactionDao.selectReactionsByIds(ids).map { it.toModel(getUser) }
    }

    /**
     * Selects all reaction ids with specific [SyncStatus].
     *
     * @param syncStatus [SyncStatus]
     */
    override suspend fun selectReactionIdsBySyncStatus(syncStatus: SyncStatus): List<Int> {
        return reactionDao.selectIdsSyncStatus(syncStatus)
    }

    /**
     * Selects all reactions with specific [SyncStatus].
     *
     * @param syncStatus [SyncStatus]
     */
    override suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction> {
        return reactionDao.selectSyncStatus(syncStatus).map { it.toModel(getUser) }
    }

    /**
     * Selects the reaction of given type to the message if exists.
     *
     * @param reactionType The type of reaction.
     * @param messageId The id of the message to which reaction belongs.
     * @param userId The id of the user who is the owner of reaction.
     *
     * @return [Reaction] if exists, null otherwise.
     */
    override suspend fun selectUserReactionToMessage(
        reactionType: String,
        messageId: String,
        userId: String,
    ): Reaction? {
        return reactionDao.selectUserReactionToMessage(
            reactionType = reactionType,
            messageId = messageId,
            userId = userId,
        )?.toModel(getUser)
    }

    /**
     * Selects all current user reactions of a message.
     *
     * @param messageId String.
     * @param userId String.
     */
    override suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> {
        return reactionDao.selectUserReactionsToMessage(messageId = messageId, userId = userId)
            .map { it.toModel(getUser) }
    }

    /**
     * Deletes a reaction.
     *
     * @param reaction [Reaction]
     */
    override suspend fun deleteReaction(reaction: Reaction) {
        reactionDao.delete(reaction.toEntity())
    }

    override suspend fun clear() {
        reactionDao.deleteAll()
    }
}
