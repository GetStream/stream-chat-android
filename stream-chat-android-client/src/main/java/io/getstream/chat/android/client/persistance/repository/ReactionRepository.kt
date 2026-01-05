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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

/**
 * Repository to read and write reactions.
 */
public interface ReactionRepository {

    /**
     * Inserts a reaction.
     *
     * @param reaction [Reaction]
     */
    public suspend fun insertReaction(reaction: Reaction)

    /**
     * Updates the Reaction.deletedAt for reactions of a message.
     *
     * @param userId String.
     * @param messageId String.
     * @param deletedAt Date.
     */
    public suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date)

    /**
     * Selects reaction with specified [id].
     *
     * @param id A reaction id to search for.
     */
    public suspend fun selectReactionById(id: Int): Reaction?

    /**
     * Selects all reactions with specified [ids]
     *
     * @param ids A list of reaction id to search for.
     */
    public suspend fun selectReactionsByIds(ids: List<Int>): List<Reaction>

    /**
     * Selects all reaction ids with specific [SyncStatus].
     *
     * @param syncStatus [SyncStatus]
     */
    public suspend fun selectReactionIdsBySyncStatus(syncStatus: SyncStatus): List<Int>

    /**
     * Selects all reactions with specific [SyncStatus]
     *
     * @param syncStatus [SyncStatus]
     */
    public suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction>

    /**
     * Selects the reaction of given type to the message if exists.
     *
     * @param reactionType The type of reaction.
     * @param messageId The id of the message to which reaction belongs.
     * @param userId The id of the user who is the owner of reaction.
     *
     * @return [Reaction] if exists, null otherwise.
     */
    public suspend fun selectUserReactionToMessage(reactionType: String, messageId: String, userId: String): Reaction?

    /**
     * Selects all current user reactions of a message.
     *
     * @param messageId String.
     * @param userId String.
     */
    public suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction>

    /**
     * Deletes a reaction.
     *
     * @param reaction [Reaction]
     */
    public suspend fun deleteReaction(reaction: Reaction)

    /**
     * Clear reactions of this repository.
     */
    public suspend fun clear()
}
