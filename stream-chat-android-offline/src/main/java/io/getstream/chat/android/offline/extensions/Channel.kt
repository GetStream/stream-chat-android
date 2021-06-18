package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.message.users
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest

internal fun Channel.users(): List<User> {
    return members.map(Member::user) +
        read.map(ChannelUserRead::user) +
        createdBy +
        messages.flatMap { it.users() }
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

internal fun Channel.incrementUnreadCount(currentUserId: String) {
    read.firstOrNull { it.user.id == currentUserId }
        ?.let { it.unreadMessages++ }
}

internal fun Collection<Channel>.applyPagination(pagination: AnyChannelPaginationRequest): List<Channel> {
    return asSequence().sortedWith(pagination.sort.comparator)
        .drop(pagination.channelOffset)
        .take(pagination.channelLimit)
        .toList()
}
