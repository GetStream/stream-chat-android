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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

public interface ReactionRepository {
    public suspend fun insertReaction(reaction: Reaction)
    public suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date)
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

    public suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction>
}
