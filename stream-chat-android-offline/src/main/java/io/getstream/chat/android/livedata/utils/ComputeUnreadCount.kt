package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

internal fun computeUnreadCount(
    currentUser: User,
    read: ChannelUserRead? = null,
    messages: List<Message>? = null
): Int? {

    if (messages == null) {
        return null
    }

    if (read == null) {
        return messages.size
    }

    if (messages.size <= 1) {
        return read.unreadMessages
    }

    val lastRead = read.lastRead
        ?: throw IllegalStateException("ChannelUserRead instance for ${read.user.id} missing lastRead date")

    return messages
        .asSequence()
        .filter { it.user.id != currentUser.id } // doesn't belong to the current user
        .filter { it.deletedAt == null } // isn't deleted
        .filter { !it.silent } // isn't silent
        .count { message ->
            val messageCreatedAt =
                message.createdAt
                    ?: message.createdLocallyAt
                    ?: throw IllegalStateException("Message ${message.id} missing creation date")

            lastRead.before(messageCreatedAt)
        }
}
