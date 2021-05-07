package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction

internal fun Message.addMyReaction(reaction: Reaction, enforceUnique: Boolean = false) {
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

internal fun Message.removeMyReaction(reaction: Reaction) {
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

        val newScore = reactionScores.getOrElse(type) { 0 } - reactions.sumBy { it.score }
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
