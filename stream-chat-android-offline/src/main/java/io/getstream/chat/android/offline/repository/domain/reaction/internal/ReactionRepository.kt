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

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistence.repository.ReactionRepository
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

/**
 * We don't do any caching on reactions since usage is infrequent.
 */
internal class ReactionRepositoryImpl(
    private val reactionDao: ReactionDao,
    private val getUser: suspend (userId: String) -> User,
) : ReactionRepository {

    override suspend fun insertReaction(reaction: Reaction) {
        require(reaction.messageId.isNotEmpty()) { "message id can't be empty when creating a reaction" }
        require(reaction.type.isNotEmpty()) { "type can't be empty when creating a reaction" }
        require(reaction.userId.isNotEmpty()) { "user id can't be empty when creating a reaction" }

        reactionDao.insert(reaction.toEntity())
    }

    override suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) {
        reactionDao.setDeleteAt(userId, messageId, deletedAt)
    }

    override suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction> {
        return reactionDao.selectSyncStatus(syncStatus).map { it.toModel(getUser) }
    }

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

    override suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> {
        return reactionDao.selectUserReactionsToMessage(messageId = messageId, userId = userId)
            .map { it.toModel(getUser) }
    }
}
