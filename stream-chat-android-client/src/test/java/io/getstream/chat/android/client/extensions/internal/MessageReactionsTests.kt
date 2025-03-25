/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageReactionsTests {

    /** This me use [addMyReactionArguments] as a source of arguments. */
    @ParameterizedTest
    @MethodSource("addMyReactionArguments")
    fun `Adding reactions to a message should return a copy of it with the update reaction list`(
        initialMessage: Message,
        newReaction: Reaction,
        enforceUnique: Boolean,
        expectedMessage: Message,
    ) {
        val updatedMessage = initialMessage.addMyReaction(newReaction, enforceUnique)

        updatedMessage.latestReactions `should be equal to` expectedMessage.latestReactions
        updatedMessage.ownReactions `should be equal to` expectedMessage.ownReactions
        updatedMessage.reactionCounts `should be equal to` expectedMessage.reactionCounts
        updatedMessage.reactionScores `should be equal to` expectedMessage.reactionScores
        updatedMessage `should be equal to` expectedMessage
    }

    /** This method uses [removeMyReactionArguments] as a source of arguments. */
    @ParameterizedTest
    @MethodSource("removeMyReactionArguments")
    fun `Removing reactions from a message should return a copy of it with the updated reaction list`(
        initialMessage: Message,
        reactionToRemove: Reaction,
        expectedMessage: Message,
    ) {
        val updatedMessage = initialMessage.removeMyReaction(reactionToRemove)

        updatedMessage.latestReactions `should be equal to` expectedMessage.latestReactions
        updatedMessage.ownReactions `should be equal to` expectedMessage.ownReactions
        updatedMessage.reactionCounts `should be equal to` expectedMessage.reactionCounts
        updatedMessage.reactionScores `should be equal to` expectedMessage.reactionScores
        updatedMessage `should be equal to` expectedMessage
    }

    companion object {
        private val messageId = randomString()
        private val currentUserId = randomString()
        private val otherUserId = randomString()

        private fun createOwnReaction(type: String) = randomReaction(
            messageId = messageId,
            userId = currentUserId,
            type = type,
            score = positiveRandomInt(20),
        )

        private fun createOtherReaction(type: String) = randomReaction(
            messageId = messageId,
            userId = otherUserId,
            type = type,
            score = positiveRandomInt(20),
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun addMyReactionArguments(): List<Arguments> = listOf(
            run {
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = emptyList(),
                    ownReactions = emptyList(),
                    reactionCounts = emptyMap(),
                    reactionScores = emptyMap(),
                )
                val reaction = createOwnReaction("like")
                Arguments.of(
                    initialMessage,
                    reaction,
                    true,
                    initialMessage.copy(
                        latestReactions = listOf(reaction),
                        ownReactions = listOf(reaction),
                        reactionCounts = mapOf(reaction.type to 1),
                        reactionScores = mapOf(reaction.type to reaction.score),
                    ),
                )
            },
            run {
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = emptyList(),
                    ownReactions = emptyList(),
                    reactionCounts = emptyMap(),
                    reactionScores = emptyMap(),
                )
                val reaction = createOwnReaction("like")
                Arguments.of(
                    initialMessage,
                    reaction,
                    false,
                    initialMessage.copy(
                        latestReactions = listOf(reaction),
                        ownReactions = listOf(reaction),
                        reactionCounts = mapOf(reaction.type to 1),
                        reactionScores = mapOf(reaction.type to reaction.score),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val otherReactions = listOf(createOtherReaction(reactionType))
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = otherReactions,
                    ownReactions = emptyList(),
                    reactionCounts = mapOf(reactionType to 1),
                    reactionScores = mapOf(reactionType to otherReactions.first().score),
                )
                val reaction = createOwnReaction(reactionType)
                val latestReactions = otherReactions + reaction
                Arguments.of(
                    initialMessage,
                    reaction,
                    true,
                    initialMessage.copy(
                        latestReactions = latestReactions,
                        ownReactions = listOf(reaction),
                        reactionCounts = mapOf(reactionType to latestReactions.size),
                        reactionScores = mapOf(reactionType to latestReactions.sumOf { it.score }),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val otherReactions = listOf(createOtherReaction(reactionType))
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = otherReactions,
                    ownReactions = emptyList(),
                    reactionCounts = mapOf(reactionType to 1),
                    reactionScores = mapOf(reactionType to otherReactions.first().score),
                )
                val reaction = createOwnReaction(reactionType)
                val latestReactions = otherReactions + reaction
                Arguments.of(
                    initialMessage,
                    reaction,
                    false,
                    initialMessage.copy(
                        latestReactions = latestReactions,
                        ownReactions = listOf(reaction),
                        reactionCounts = mapOf(reactionType to latestReactions.size),
                        reactionScores = mapOf(reactionType to latestReactions.sumOf { it.score }),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val newReactionType = "haha"
                val otherReactions = listOf(createOtherReaction(reactionType))
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = otherReactions,
                    ownReactions = emptyList(),
                    reactionCounts = mapOf(reactionType to 1),
                    reactionScores = mapOf(reactionType to otherReactions.first().score),
                )
                val reaction = createOwnReaction(newReactionType)
                val latestReactions = otherReactions + reaction
                Arguments.of(
                    initialMessage,
                    reaction,
                    true,
                    initialMessage.copy(
                        latestReactions = latestReactions,
                        ownReactions = listOf(reaction),
                        reactionCounts = mapOf(reactionType to otherReactions.size, newReactionType to 1),
                        reactionScores = mapOf(
                            reactionType to otherReactions.sumOf { it.score },
                            newReactionType to reaction.score,
                        ),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val newReactionType = "haha"
                val otherReactions = listOf(createOtherReaction(reactionType))
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = otherReactions,
                    ownReactions = emptyList(),
                    reactionCounts = mapOf(reactionType to 1),
                    reactionScores = mapOf(reactionType to otherReactions.first().score),
                )
                val reaction = createOwnReaction(newReactionType)
                val latestReactions = otherReactions + reaction
                Arguments.of(
                    initialMessage,
                    reaction,
                    false,
                    initialMessage.copy(
                        latestReactions = latestReactions,
                        ownReactions = listOf(reaction),
                        reactionCounts = mapOf(reactionType to otherReactions.size, newReactionType to 1),
                        reactionScores = mapOf(
                            reactionType to otherReactions.sumOf { it.score },
                            newReactionType to reaction.score,
                        ),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val newReactionType = "haha"
                val ownReactions = listOf(createOwnReaction(reactionType))
                val otherReactions = listOf(createOtherReaction(reactionType)) + ownReactions
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = otherReactions,
                    ownReactions = ownReactions,
                    reactionCounts = mapOf(reactionType to otherReactions.size),
                    reactionScores = mapOf(reactionType to otherReactions.sumOf { it.score }),
                )
                val reaction = createOwnReaction(newReactionType)
                val latestReactions = otherReactions - ownReactions + reaction
                Arguments.of(
                    initialMessage,
                    reaction,
                    true,
                    initialMessage.copy(
                        latestReactions = latestReactions,
                        ownReactions = listOf(reaction),
                        reactionCounts = mapOf(reactionType to otherReactions.size - 1, newReactionType to 1),
                        reactionScores = mapOf(
                            reactionType to otherReactions.sumOf { it.score } - ownReactions.first().score,
                            newReactionType to reaction.score,
                        ),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val newReactionType = "haha"
                val ownReactions = listOf(createOwnReaction(reactionType))
                val otherReactions = listOf(createOtherReaction(reactionType)) + ownReactions
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = otherReactions,
                    ownReactions = ownReactions,
                    reactionCounts = mapOf(reactionType to otherReactions.size),
                    reactionScores = mapOf(reactionType to otherReactions.sumOf { it.score }),
                )
                val reaction = createOwnReaction(newReactionType)
                val latestReactions = otherReactions + reaction
                Arguments.of(
                    initialMessage,
                    reaction,
                    false,
                    initialMessage.copy(
                        latestReactions = latestReactions,
                        ownReactions = ownReactions + reaction,
                        reactionCounts = mapOf(reactionType to otherReactions.size, newReactionType to 1),
                        reactionScores = mapOf(
                            reactionType to otherReactions.sumOf { it.score },
                            newReactionType to reaction.score,
                        ),
                    ),
                )
            },
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun removeMyReactionArguments(): List<Arguments> = listOf(
            run {
                val reactionType = "like"
                val ownReaction = createOwnReaction(reactionType)
                val otherReaction = createOtherReaction(reactionType)
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = listOf(ownReaction, otherReaction),
                    ownReactions = listOf(ownReaction),
                    reactionCounts = mapOf(reactionType to 2),
                    reactionScores = mapOf(reactionType to ownReaction.score + otherReaction.score),
                )
                Arguments.of(
                    initialMessage,
                    ownReaction,
                    initialMessage.copy(
                        latestReactions = listOf(otherReaction),
                        ownReactions = emptyList(),
                        reactionCounts = mapOf(reactionType to 1),
                        reactionScores = mapOf(reactionType to otherReaction.score),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val ownReaction = createOwnReaction(reactionType)
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = listOf(ownReaction),
                    ownReactions = listOf(ownReaction),
                    reactionCounts = mapOf(reactionType to 1),
                    reactionScores = mapOf(reactionType to ownReaction.score),
                )
                Arguments.of(
                    initialMessage,
                    ownReaction,
                    initialMessage.copy(
                        latestReactions = emptyList(),
                        ownReactions = emptyList(),
                        reactionCounts = emptyMap(),
                        reactionScores = emptyMap(),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val ownReaction = createOwnReaction(reactionType)
                val otherReaction = createOtherReaction(reactionType)
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = listOf(ownReaction, otherReaction),
                    ownReactions = listOf(ownReaction),
                    reactionCounts = mapOf(reactionType to 2),
                    reactionScores = mapOf(reactionType to ownReaction.score + otherReaction.score),
                )
                Arguments.of(
                    initialMessage,
                    createOwnReaction(reactionType), // Different reaction with same type and userId
                    initialMessage.copy(
                        latestReactions = listOf(otherReaction),
                        ownReactions = emptyList(),
                        reactionCounts = mapOf(reactionType to 1),
                        reactionScores = mapOf(reactionType to otherReaction.score),
                    ),
                )
            },
            run {
                val reactionType = "like"
                val ownReaction = createOwnReaction(reactionType)
                val otherReaction = createOtherReaction(reactionType)
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = listOf(ownReaction, otherReaction),
                    ownReactions = listOf(ownReaction),
                    reactionCounts = mapOf(reactionType to 2),
                    reactionScores = mapOf(reactionType to ownReaction.score + otherReaction.score),
                )
                Arguments.of(
                    initialMessage,
                    createOwnReaction("haha"), // Different reaction type
                    initialMessage, // Should remain unchanged
                )
            },
        )
    }
}
