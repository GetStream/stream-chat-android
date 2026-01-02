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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import io.getstream.chat.android.client.internal.offline.randomReactionGroupEntity
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.randomReactionGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ReactionGroupMapperKtTest {

    @Test
    fun `Should map ReactionGroupEntity to ReactionGroup correctly`() {
        val reactionGroupEntity = randomReactionGroupEntity()
        val expectedReactionGroup = ReactionGroup(
            type = reactionGroupEntity.type,
            count = reactionGroupEntity.count,
            sumScore = reactionGroupEntity.sumScore,
            firstReactionAt = reactionGroupEntity.firstReactionAt,
            lastReactionAt = reactionGroupEntity.lastReactionAt,
        )

        val result = reactionGroupEntity.toModel()

        assertEquals(expectedReactionGroup, result)
    }

    @Test
    fun `Should map ReactionGroup to ReactionGroupEntity correctly`() {
        val reactionGroup = randomReactionGroup()
        val expectedReactionGroupEntity = ReactionGroupEntity(
            type = reactionGroup.type,
            count = reactionGroup.count,
            sumScore = reactionGroup.sumScore,
            firstReactionAt = reactionGroup.firstReactionAt,
            lastReactionAt = reactionGroup.lastReactionAt,
        )

        val result = reactionGroup.toEntity()

        assertEquals(expectedReactionGroupEntity, result)
    }
}
