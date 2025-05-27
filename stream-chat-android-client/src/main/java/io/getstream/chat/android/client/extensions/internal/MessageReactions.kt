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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import java.util.Date

/**
 * Add a [Reaction] created by the currently logged user to the given [Message].
 *
 * @param reaction The reaction to add.
 * @param enforceUnique If true, the reaction will be added only if the user hasn't already added a reaction of the same
 * type.
 */
@InternalStreamChatApi
public fun Message.addMyReaction(reaction: Reaction, enforceUnique: Boolean = false): Message {
    // Remove all previous own reactions if enforceUnique is true
    val message = if (enforceUnique) {
        removeReactions(reaction.userId)
    } else {
        this
    }
    // Add the new reaction
    // Reaction score
    val currentScore = message.reactionScores[reaction.type] ?: 0
    val newScore = currentScore + reaction.score
    val newReactionScores = message.reactionScores + (reaction.type to newScore)
    // Reaction count
    val currentCount = message.reactionCounts[reaction.type] ?: 0
    val newCount = currentCount + 1
    val newReactionCounts = message.reactionCounts + (reaction.type to newCount)
    // Reaction groups
    val currentGroup = message.reactionGroups[reaction.type]
    val newGroup = currentGroup?.copy(
        sumScore = currentGroup.sumScore + reaction.score,
        count = currentGroup.count + 1,
        lastReactionAt = reaction.createdAt ?: reaction.createdLocallyAt ?: Date(),
    ) ?: ReactionGroup(
        type = reaction.type,
        count = 1,
        sumScore = reaction.score,
        firstReactionAt = reaction.createdAt ?: reaction.createdLocallyAt ?: Date(),
        lastReactionAt = reaction.createdAt ?: reaction.createdLocallyAt ?: Date(),
    )
    val newReactionGroups = message.reactionGroups + (reaction.type to newGroup)
    // Latest reactions
    val newLatestReactions = message.latestReactions + reaction
    // Own reactions
    val newOwnReactions = message.ownReactions + reaction
    // Return the updated message
    return message.copy(
        latestReactions = newLatestReactions,
        ownReactions = newOwnReactions,
        reactionCounts = newReactionCounts,
        reactionScores = newReactionScores,
        reactionGroups = newReactionGroups,
    )
}

/**
 * Remove a [Reaction] created by the currently logged user from the given [Message].
 *
 * @param reaction The reaction to remove.
 */
@InternalStreamChatApi
public fun Message.removeMyReaction(reaction: Reaction): Message {
    return removeReactions(userId = reaction.userId, type = reaction.type)
}

/**
 * Removes reactions from a message.
 * When [type] is provided, removes reactions for the [userId] from the provided type.
 * When [type] is null, removes all reactions for the [userId].
 */
private fun Message.removeReactions(
    userId: String,
    type: String? = null,
): Message {
    val reactionsToRemove = if (type != null) {
        // Remove specific reactions
        ownReactions
            .filter { it.type == type && it.userId == userId }
            .toSet()
    } else {
        // Remove all user reactions
        ownReactions.toSet()
    }

    val newReactionScores = reactionScores.toMutableMap()
    val newReactionCounts = reactionCounts.toMutableMap()
    val newReactionGroups = reactionGroups.toMutableMap()
    reactionsToRemove.forEach { reaction ->
        // Decrease reaction score
        reactionScores[reaction.type]?.let { score ->
            val newScore = score - reaction.score
            if (newScore > 0) {
                newReactionScores[reaction.type] = newScore
            } else {
                newReactionScores.remove(reaction.type)
            }
        }
        // Decrement reaction count
        reactionCounts[reaction.type]?.let { count ->
            val newCount = count - 1
            if (newCount > 0) {
                newReactionCounts[reaction.type] = newCount
            } else {
                newReactionCounts.remove(reaction.type)
            }
        }
        // Update reaction groups
        reactionGroups[reaction.type]?.let { group ->
            val newGroup = group.copy(
                sumScore = group.sumScore - reaction.score,
                count = group.count - 1,
            )
            if (newGroup.sumScore > 0 && newGroup.count > 0) {
                newReactionGroups[reaction.type] = newGroup
            } else {
                newReactionGroups.remove(reaction.type)
            }
        }
    }

    val newOwnReactions = if (type != null) {
        ownReactions - reactionsToRemove
    } else {
        emptyList() // Clear all when removing all user reactions
    }

    val newLatestReactions = if (type != null) {
        latestReactions.filterNot { it.type == type && it.userId == userId }
    } else {
        latestReactions.filterNot { it.userId == userId }
    }

    return this.copy(
        ownReactions = newOwnReactions,
        latestReactions = newLatestReactions,
        reactionCounts = newReactionCounts,
        reactionScores = newReactionScores,
        reactionGroups = newReactionGroups,
    )
}
