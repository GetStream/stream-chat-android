package io.getstream.chat.android.livedata.extensions

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User

// TODO: move these to the LLC at some point
fun ChatEvent.isChannelEvent(): Boolean = !this.cid.isNullOrEmpty() && this.cid != "*"

/**
 * cid is sometimes devent as event.cid other times as event.channel.cid
 */
fun ChatEvent.getCid(): String? {

    var cid = this.cid

    if (cid.isNullOrEmpty()) {
        cid = this.channel?.cid
    }

    return cid
}

/**
 * cid is sometimes devent as message.cid other times as message.channel.cid
 */
fun Message.getCid(): String =
    if (this.cid.isEmpty()) {
        this.channel.cid
    } else {
        this.cid
    }


fun Message.users(): List<User> {
    val users = mutableListOf<User>()
    users.add(this.user)
    for (reaction in this.latestReactions) {
        reaction.user?.let { users.add(it) }
    }
    return users
}

fun Channel.users(): List<User> {
    val users = mutableListOf<User>()
    users.add(this.createdBy)
    for (member in this.members) {
        users.add(member.user)
    }
    for (read in this.read) {
        users.add(read.user)
    }
    return users
}

fun Message.addReaction(reaction: Reaction, isMine: Boolean) {
    // add to own reactions
    if (isMine) {
        this.ownReactions.add(reaction)
    }

    // add to latest reactions
    this.latestReactions.add(reaction)

    // update the count
    val currentCount = this.reactionCounts.getOrElse(reaction.type) { 0 }
    this.reactionCounts[reaction.type] = currentCount + 1
    // update the score
    val currentScore = this.reactionScores.getOrElse(reaction.type) { 0 }
    this.reactionScores[reaction.type] = currentScore + reaction.score
}

fun Message.removeReaction(reaction: Reaction, updateCounts: Boolean) {

    val removed1 = this.ownReactions.remove(reaction)
    val removed2 = this.latestReactions.remove(reaction)
    if (updateCounts) {
        val shouldDecrement = removed1 || removed2 || this.latestReactions.size >= 15
        if (shouldDecrement) {
            val currentCount = this.reactionCounts.getOrElse(reaction.type) { 1 }
            this.reactionCounts[reaction.type] = currentCount - 1
            val currentScore = this.reactionScores.getOrElse(reaction.type) { 1 }
            this.reactionScores[reaction.type] = currentScore - reaction.score
        }
    }
}

/**
 * Returns true if an error is a permanent failure instead of a temporary one (broken network, 500, rate limit etc.)
 */
fun ChatError.isPermanent(): Boolean {
    // errors without a networkError.streamCode should always be considered temporary
    // errors with networkError.statusCode 429 should be considered temporary
    // everything else is a permanent error
    var isPermanent = true
    if (this is ChatNetworkError) {
        val networkError: ChatNetworkError = this
        if (networkError.statusCode == 429) {
            isPermanent = false
        } else if (networkError.streamCode == 0) {
            isPermanent = false
        }
    } else {
        isPermanent = false
    }
    return isPermanent
}
