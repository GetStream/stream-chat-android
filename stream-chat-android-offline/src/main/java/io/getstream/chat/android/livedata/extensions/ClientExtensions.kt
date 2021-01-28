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
