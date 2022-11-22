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

@InternalStreamChatApi
public fun Message.addMyReaction(reaction: Reaction, enforceUnique: Boolean = false) {
    updateReactions {
        if (enforceUnique) {
            clearOwnReactions(reaction.userId)
        }
        latestReactions.add(reaction)
        ownReactions.add(reaction)
        reactionCounts[reaction.type] = reactionCounts.getOrElse(reaction.type) { 0 } + 1
        reactionScores[reaction.type] = reactionScores.getOrElse(reaction.type) { 0 } + reaction.score
    }
}

@InternalStreamChatApi
public fun Message.removeMyReaction(reaction: Reaction) {
    updateReactions {
        latestReactions.removeAll { it.type == reaction.type && it.userId == reaction.userId }
        val removed = ownReactions.removeAll { it.type == reaction.type && it.userId == reaction.userId }

        if (removed) {
            val newCount = reactionCounts.getOrElse(reaction.type) { 1 } - 1
            if (newCount > 0) {
                reactionCounts[reaction.type] = newCount
            } else {
                reactionCounts.remove(reaction.type)
            }

            val newScore = reactionScores.getOrElse(reaction.type) { 1 } - reaction.score
            if (newScore > 0) {
                reactionScores[reaction.type] = newScore
            } else {
                reactionScores.remove(reaction.type)
            }
        }
    }
}

private fun ReactionData.clearOwnReactions(userId: String) {
    latestReactions.removeAll { it.userId == userId }

    ownReactions.groupBy { it.type }.forEach { (type, reactions) ->
        val newCount = reactionCounts.getOrElse(type) { 0 } - reactions.size
        if (newCount > 0) {
            reactionCounts[type] = newCount
        } else {
            reactionCounts.remove(type)
        }

        val newScore = reactionScores.getOrElse(type) { 0 } - reactions.sumOf { it.score }
        if (newScore > 0) {
            reactionScores[type] = newScore
        } else {
            reactionScores.remove(type)
        }
    }
    ownReactions.clear()
}

private inline fun Message.updateReactions(actions: ReactionData.() -> Unit) {
    // copy objects so that diff utils can notice the change
    val reactionData = ReactionData(
        reactionCounts.toMutableMap(),
        reactionScores.toMutableMap(),
        latestReactions.toMutableList(),
        ownReactions.toMutableList(),
    )
    reactionData.actions()
    reactionCounts = reactionData.reactionCounts
    reactionScores = reactionData.reactionScores
    latestReactions = reactionData.latestReactions
    ownReactions = reactionData.ownReactions
}

private data class ReactionData(
    val reactionCounts: MutableMap<String, Int>,
    val reactionScores: MutableMap<String, Int>,
    val latestReactions: MutableList<Reaction>,
    val ownReactions: MutableList<Reaction>,
)
