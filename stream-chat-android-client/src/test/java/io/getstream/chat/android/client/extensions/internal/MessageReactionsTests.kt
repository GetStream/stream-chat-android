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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageReactionsTests {

    /** This method uses [addMyReactionArguments] as a source of arguments. */
    @ParameterizedTest
    @MethodSource("addMyReactionArguments")
    fun `Adding reactions to a message should return a copy of it with the update reaction list`(
        initialMessage: Message,
        newReaction: Reaction,
        enforceUnique: Boolean,
        expectedMessage: Message,
    ) {
        val updatedMessage = initialMessage.addMyReaction(newReaction, enforceUnique)

        Assertions.assertEquals(expectedMessage.latestReactions, updatedMessage.latestReactions)
        Assertions.assertEquals(expectedMessage.ownReactions, updatedMessage.ownReactions)
        Assertions.assertEquals(expectedMessage.reactionCounts, updatedMessage.reactionCounts)
        Assertions.assertEquals(expectedMessage.reactionScores, updatedMessage.reactionScores)
        Assertions.assertEquals(expectedMessage.reactionGroups, updatedMessage.reactionGroups)
        Assertions.assertEquals(expectedMessage, updatedMessage)
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

        Assertions.assertEquals(expectedMessage.latestReactions, updatedMessage.latestReactions)
        Assertions.assertEquals(expectedMessage.ownReactions, updatedMessage.ownReactions)
        Assertions.assertEquals(expectedMessage.reactionCounts, updatedMessage.reactionCounts)
        Assertions.assertEquals(expectedMessage.reactionScores, updatedMessage.reactionScores)
        Assertions.assertEquals(expectedMessage.reactionGroups, updatedMessage.reactionGroups)
        Assertions.assertEquals(expectedMessage, updatedMessage)
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
            // Adding a reaction to an empty message, enforceUnique = true
            run {
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = emptyList(),
                    ownReactions = emptyList(),
                    reactionCounts = emptyMap(),
                    reactionScores = emptyMap(),
                    reactionGroups = emptyMap(),
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
                        reactionGroups = mapOf(
                            reaction.type to ReactionGroup(
                                type = reaction.type,
                                count = 1,
                                sumScore = reaction.score,
                                firstReactionAt = reaction.createdAt!!,
                                lastReactionAt = reaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Adding a reaction to an empty message, enforceUnique = false
            run {
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = emptyList(),
                    ownReactions = emptyList(),
                    reactionCounts = emptyMap(),
                    reactionScores = emptyMap(),
                    reactionGroups = emptyMap(),
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
                        reactionGroups = mapOf(
                            reaction.type to ReactionGroup(
                                type = reaction.type,
                                count = 1,
                                sumScore = reaction.score,
                                firstReactionAt = reaction.createdAt!!,
                                lastReactionAt = reaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Adding a reaction of type "like" to a message with other "like" reaction, enforceUnique = true
            run {
                val reactionType = "like"
                val otherReactions = listOf(createOtherReaction(reactionType))
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = otherReactions,
                    ownReactions = emptyList(),
                    reactionCounts = mapOf(reactionType to 1),
                    reactionScores = mapOf(reactionType to otherReactions.first().score),
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 1,
                            sumScore = otherReactions.sumOf { it.score },
                            firstReactionAt = otherReactions.first().createdAt!!,
                            lastReactionAt = otherReactions.first().createdAt!!,
                        ),
                    ),
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
                        reactionGroups = mapOf(
                            reactionType to ReactionGroup(
                                type = reactionType,
                                count = 2,
                                sumScore = latestReactions.sumOf { it.score },
                                firstReactionAt = otherReactions.first().createdAt!!,
                                lastReactionAt = reaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Adding a reaction of type "like" to a message with other "like" reaction, enforceUnique = false
            run {
                val reactionType = "like"
                val otherReactions = listOf(createOtherReaction(reactionType))
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = otherReactions,
                    ownReactions = emptyList(),
                    reactionCounts = mapOf(reactionType to 1),
                    reactionScores = mapOf(reactionType to otherReactions.first().score),
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 1,
                            sumScore = otherReactions.sumOf { it.score },
                            firstReactionAt = otherReactions.first().createdAt!!,
                            lastReactionAt = otherReactions.first().createdAt!!,
                        ),
                    ),
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
                        reactionGroups = mapOf(
                            reactionType to ReactionGroup(
                                type = reactionType,
                                count = 2,
                                sumScore = latestReactions.sumOf { it.score },
                                firstReactionAt = otherReactions.first().createdAt!!,
                                lastReactionAt = reaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Adding a reaction of type "haha" to a message with other "type" reaction, enforceUnique = true
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
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 1,
                            sumScore = otherReactions.first().score,
                            firstReactionAt = otherReactions.first().createdAt!!,
                            lastReactionAt = otherReactions.first().createdAt!!,
                        ),
                    ),
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
                        reactionGroups = mapOf(
                            reactionType to ReactionGroup(
                                type = reactionType,
                                count = 1,
                                sumScore = otherReactions.first().score,
                                firstReactionAt = otherReactions.first().createdAt!!,
                                lastReactionAt = otherReactions.first().createdAt!!,
                            ),
                            newReactionType to ReactionGroup(
                                type = newReactionType,
                                count = 1,
                                sumScore = reaction.score,
                                firstReactionAt = reaction.createdAt!!,
                                lastReactionAt = reaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Adding a reaction of type "haha" to a message with other "type" reaction, enforceUnique = false
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
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 1,
                            sumScore = otherReactions.first().score,
                            firstReactionAt = otherReactions.first().createdAt!!,
                            lastReactionAt = otherReactions.first().createdAt!!,
                        ),
                    ),
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
                        reactionGroups = mapOf(
                            reactionType to ReactionGroup(
                                type = reactionType,
                                count = 1,
                                sumScore = otherReactions.first().score,
                                firstReactionAt = otherReactions.first().createdAt!!,
                                lastReactionAt = otherReactions.first().createdAt!!,
                            ),
                            newReactionType to ReactionGroup(
                                type = newReactionType,
                                count = 1,
                                sumScore = reaction.score,
                                firstReactionAt = reaction.createdAt!!,
                                lastReactionAt = reaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Adding a reaction of type "haha" to a message with own "like" reaction, enforceUnique = true
            // (Existing "like" is overridden by "haha")
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
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 2,
                            sumScore = otherReactions.sumOf { it.score },
                            firstReactionAt = otherReactions.first().createdAt!!,
                            lastReactionAt = otherReactions.first().createdAt!!,
                        ),
                    ),
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
                        reactionGroups = mapOf(
                            reactionType to ReactionGroup(
                                type = reactionType,
                                count = 1,
                                sumScore = otherReactions.sumOf { it.score } - ownReactions.first().score,
                                firstReactionAt = otherReactions.first().createdAt!!,
                                lastReactionAt = otherReactions.first().createdAt!!,
                            ),
                            newReactionType to ReactionGroup(
                                type = newReactionType,
                                count = 1,
                                sumScore = reaction.score,
                                firstReactionAt = reaction.createdAt!!,
                                lastReactionAt = reaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Adding a reaction of type "haha" to a message with own "like" reaction, enforceUnique = false
            // (New "haha" is added in addition to the existing "like")
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
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 2,
                            sumScore = otherReactions.sumOf { it.score },
                            firstReactionAt = otherReactions.first().createdAt!!,
                            lastReactionAt = otherReactions.first().createdAt!!,
                        ),
                    ),
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
                        reactionGroups = mapOf(
                            reactionType to ReactionGroup(
                                type = reactionType,
                                count = 2,
                                sumScore = otherReactions.sumOf { it.score },
                                firstReactionAt = otherReactions.first().createdAt!!,
                                lastReactionAt = otherReactions.first().createdAt!!,
                            ),
                            newReactionType to ReactionGroup(
                                type = newReactionType,
                                count = 1,
                                sumScore = reaction.score,
                                firstReactionAt = reaction.createdAt!!,
                                lastReactionAt = reaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
        )

        @JvmStatic
        @Suppress("LongMethod")
        fun removeMyReactionArguments(): List<Arguments> = listOf(
            // Removing a reaction from a message with multiple reaction
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
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 2,
                            sumScore = ownReaction.score + otherReaction.score,
                            firstReactionAt = ownReaction.createdAt!!,
                            lastReactionAt = otherReaction.createdAt!!,
                        ),
                    ),
                )
                Arguments.of(
                    initialMessage,
                    ownReaction,
                    initialMessage.copy(
                        latestReactions = listOf(otherReaction),
                        ownReactions = emptyList(),
                        reactionCounts = mapOf(reactionType to 1),
                        reactionScores = mapOf(reactionType to otherReaction.score),
                        reactionGroups = mapOf(
                            reactionType to ReactionGroup(
                                type = reactionType,
                                count = 1,
                                sumScore = otherReaction.score,
                                firstReactionAt = ownReaction.createdAt!!,
                                lastReactionAt = otherReaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Removing a reaction from a message with only one (own) reaction
            run {
                val reactionType = "like"
                val ownReaction = createOwnReaction(reactionType)
                val initialMessage = randomMessage(
                    id = messageId,
                    latestReactions = listOf(ownReaction),
                    ownReactions = listOf(ownReaction),
                    reactionCounts = mapOf(reactionType to 1),
                    reactionScores = mapOf(reactionType to ownReaction.score),
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 1,
                            sumScore = ownReaction.score,
                            firstReactionAt = ownReaction.createdAt!!,
                            lastReactionAt = ownReaction.createdAt!!,
                        ),
                    ),
                )
                Arguments.of(
                    initialMessage,
                    ownReaction,
                    initialMessage.copy(
                        latestReactions = emptyList(),
                        ownReactions = emptyList(),
                        reactionCounts = emptyMap(),
                        reactionScores = emptyMap(),
                        reactionGroups = emptyMap(),
                    ),
                )
            },
            // Removing a reaction from a message with multiple reactions (own and others)
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
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 2,
                            sumScore = ownReaction.score + otherReaction.score,
                            firstReactionAt = ownReaction.createdAt!!,
                            lastReactionAt = otherReaction.createdAt!!,
                        ),
                    ),
                )
                Arguments.of(
                    initialMessage,
                    createOwnReaction(reactionType), // Different reaction with same type and userId
                    initialMessage.copy(
                        latestReactions = listOf(otherReaction),
                        ownReactions = emptyList(),
                        reactionCounts = mapOf(reactionType to 1),
                        reactionScores = mapOf(reactionType to otherReaction.score),
                        reactionGroups = mapOf(
                            reactionType to ReactionGroup(
                                type = reactionType,
                                count = 1,
                                sumScore = otherReaction.score,
                                firstReactionAt = ownReaction.createdAt!!,
                                lastReactionAt = otherReaction.createdAt!!,
                            ),
                        ),
                    ),
                )
            },
            // Attempt to remove a reaction which doesn't exist in the message
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
                    reactionGroups = mapOf(
                        reactionType to ReactionGroup(
                            type = reactionType,
                            count = 2,
                            sumScore = ownReaction.score + otherReaction.score,
                            firstReactionAt = ownReaction.createdAt!!,
                            lastReactionAt = otherReaction.createdAt!!,
                        ),
                    ),
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
