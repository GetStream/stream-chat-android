/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.persistance.repository.noop

import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

/**
 * No-Op ReactionRepository.
 */
internal object NoOpReactionRepository : ReactionRepository {
    override suspend fun insertReaction(reaction: Reaction) { /* No-Op */ }
    override suspend fun selectReactionById(id: Int): Reaction? = null
    override suspend fun selectReactionsByIds(ids: List<Int>): List<Reaction> = emptyList()
    override suspend fun selectReactionIdsBySyncStatus(syncStatus: SyncStatus): List<Int> = emptyList()
    override suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction> = emptyList()
    override suspend fun deleteReaction(reaction: Reaction) { /* No-Op */ }
    override suspend fun clear() { /* No-Op */ }

    override suspend fun updateReactionsForMessageByDeletedDate(
        userId: String,
        messageId: String,
        deletedAt: Date,
    ) { /* No-Op */ }

    override suspend fun selectUserReactionToMessage(
        reactionType: String,
        messageId: String,
        userId: String,
    ): Reaction? = null

    override suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> = emptyList()
}
