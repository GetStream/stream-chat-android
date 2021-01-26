package io.getstream.chat.android.livedata.extensions

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest

internal fun Message.users(): List<User> =
    latestReactions.mapNotNull(Reaction::user) + user + (replyTo?.users().orEmpty())

internal fun Channel.users(): List<User> = members.map(Member::user) +
    read.map(ChannelUserRead::user) +
    createdBy +
    messages.flatMap { it.users() }

private fun Message.clearOwnReactions(userId: String) {
    // remove own reactions from latest reactions
    latestReactions.removeAll { reaction -> reaction.userId == userId }

    // update counts
    ownReactions.groupBy { it.type }.forEach { (type, reactions) ->
        // update the count
        val newCount = reactionCounts.getOrElse(type) { 0 } - reactions.size
        if (newCount <= 0) {
            reactionCounts.remove(type)
        } else {
            reactionCounts[type] = newCount
        }

        // update the score
        val newScore = reactionScores.getOrElse(type) { 0 } - reactions.sumBy { it.score }
        if (newScore <= 0) {
            reactionScores.remove(type)
        } else {
            reactionScores[type] = newScore
        }
    }
    // clear own reactions
    ownReactions.clear()
}

internal fun Message.addReaction(reaction: Reaction, isMine: Boolean, enforceUnique: Boolean = false) {

    // add to own reactions
    if (isMine) {
        if (enforceUnique) {
            clearOwnReactions(reaction.userId)
        }
        this.ownReactions.add(reaction)
    }

    // add to latest reactions
    this.latestReactions.add(reaction)

    // update the count
    val currentCount = this.reactionCounts.getOrElse(reaction.type) { 0 }
    // copy the object so livedata's diffutils can notice a change
    this.reactionCounts = this.reactionCounts.toMutableMap()
    this.reactionCounts[reaction.type] = currentCount + 1
    // update the score
    val currentScore = this.reactionScores.getOrElse(reaction.type) { 0 }
    this.reactionScores = this.reactionScores.toMutableMap()
    this.reactionScores[reaction.type] = currentScore + reaction.score
}

internal fun Message.removeReaction(reaction: Reaction, updateCounts: Boolean) {

    val countBeforeFilter = ownReactions.size + latestReactions.size
    ownReactions =
        ownReactions.filterNot { it.type == reaction.type && it.userId == reaction.userId }
            .toMutableList()
    latestReactions =
        latestReactions.filterNot { it.type == reaction.type && it.userId == reaction.userId }
            .toMutableList()
    val countAfterFilter = ownReactions.size + latestReactions.size

    if (updateCounts) {
        val shouldDecrement =
            countBeforeFilter > countAfterFilter || this.latestReactions.size >= 15
        if (shouldDecrement) {
            this.reactionCounts = this.reactionCounts.toMutableMap()
            val currentCount = this.reactionCounts.getOrElse(reaction.type) { 1 }
            val newCount = currentCount - 1
            this.reactionCounts[reaction.type] = newCount
            if (newCount <= 0) {
                reactionCounts.remove(reaction.type)
            }
            this.reactionScores = this.reactionScores.toMutableMap()
            val currentScore = this.reactionScores.getOrElse(reaction.type) { 1 }
            val newScore = currentScore - reaction.score
            this.reactionScores[reaction.type] = newScore
            if (newScore <= 0) {
                reactionScores.remove(reaction.type)
            }
        }
    }
}

internal val Channel.lastMessage: Message?
    get() = messages.lastOrNull()

internal fun Channel.updateLastMessage(message: Message) {
    val createdAt = message.createdAt ?: message.createdLocallyAt
    val messageCreatedAt =
        checkNotNull(createdAt) { "created at cant be null, be sure to set message.createdAt" }

    val updateNeeded = message.id == lastMessage?.id
    val newLastMessage = lastMessageAt == null || messageCreatedAt.after(lastMessageAt)
    if (newLastMessage || updateNeeded) {
        lastMessageAt = messageCreatedAt
        messages = messages + message
    }
}

internal fun Channel.setMember(userId: String, member: Member?) {
    if (member == null) {
        members.firstOrNull { it.user.id == userId }?.also { foundMember ->
            members = members - foundMember
        }
    } else {
        members = members + member
    }
}

internal fun Channel.updateReads(newRead: ChannelUserRead) {
    val oldRead = read.firstOrNull { it.user == newRead.user }
    read = if (oldRead != null) {
        read - oldRead + newRead
    } else {
        read + newRead
    }
}

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_TIMEOUT = 408
private const val NETWORK_NOT_AVAILABLE = -1

/**
 * Returns true if an error is a permanent failure instead of a temporary one (broken network, 500, rate limit etc.)
 *
 * A permanent error is an error returned by Stream's API (IE a validation error on the input)
 * Any permanent error will always have a stream error code
 *
 * Temporary errors are retried. Network not being available is a common example of a temporary error.
 *
 * See the error codes here
 * https://getstream.io/chat/docs/api_errors_response/?language=js
 */
public fun ChatError.isPermanent(): Boolean {
    var isPermanent = false
    if (this is ChatNetworkError) {
        val networkError: ChatNetworkError = this
        // stream errors are mostly permanent. the exception to this are the rate limit and timeout error
        val temporaryStreamErrors = listOf(HTTP_TOO_MANY_REQUESTS, HTTP_TIMEOUT)
        if (networkError.streamCode > 0) {
            isPermanent = true
            if (networkError.statusCode in temporaryStreamErrors) {
                isPermanent = false
            }
        }
    }
    return isPermanent
}

internal fun Collection<Channel>.applyPagination(pagination: AnyChannelPaginationRequest): List<Channel> =
    asSequence().sortedWith(pagination.sort.comparator).drop(pagination.channelOffset)
        .take(pagination.channelLimit).toList()

internal fun String?.isImageMimetype() = this?.contains("image") ?: false

internal fun String?.isVideoMimetype() = this?.contains("video") ?: false
