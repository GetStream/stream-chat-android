/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.offline.randomReactionEntity
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ReactionMapperTest {

    @Test
    fun `Should map Reaction to ReactionEntity correctly`() = runTest {
        val reaction = randomReaction()

        val expectedReactionEntity = ReactionEntity(
            messageId = reaction.messageId,
            userId = reaction.fetchUserId(),
            type = reaction.type,
            score = reaction.score,
            createdAt = reaction.createdAt,
            createdLocallyAt = reaction.createdLocallyAt,
            updatedAt = reaction.updatedAt,
            deletedAt = reaction.deletedAt,
            extraData = reaction.extraData,
            syncStatus = reaction.syncStatus,
            enforceUnique = reaction.enforceUnique,
            skipPush = reaction.skipPush,
            emojiCode = reaction.emojiCode,
        )

        val result = reaction.toEntity()

        assertEquals(expectedReactionEntity, result)
    }

    @Test
    fun `Should map ReactionEntity to Reaction correctly`() = runTest {
        val user = randomUser()
        val reactionEntity = randomReactionEntity()

        val expectedReaction = Reaction(
            messageId = reactionEntity.messageId,
            type = reactionEntity.type,
            score = reactionEntity.score,
            user = user,
            extraData = reactionEntity.extraData.toMutableMap(),
            createdAt = reactionEntity.createdAt,
            createdLocallyAt = reactionEntity.createdLocallyAt,
            updatedAt = reactionEntity.updatedAt,
            deletedAt = reactionEntity.deletedAt,
            syncStatus = reactionEntity.syncStatus,
            userId = reactionEntity.userId,
            enforceUnique = reactionEntity.enforceUnique,
            skipPush = reactionEntity.skipPush,
            emojiCode = reactionEntity.emojiCode,
        )

        val result = reactionEntity.toModel { user }

        assertEquals(expectedReaction, result)
    }
}
