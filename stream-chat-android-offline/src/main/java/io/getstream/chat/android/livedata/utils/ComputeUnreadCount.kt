package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

internal fun computeUnreadCount(
    currentUser: User,
    read: ChannelUserRead? = null,
    messages: List<Message> = emptyList(),
): Int {
    val logger = ChatLogger.get("computeUnreadCount")

    val validMessages = messages
        .asSequence()
        .filter { it.user.id != currentUser.id } // doesn't belong to the current user
        .filter { it.deletedAt == null } // isn't deleted
        .filter { !it.silent } // isn't silent
        .toList()

    if (read == null) {
        logger.logD("Read null; treating as if no reads and all messages unread (${validMessages.size})")
        return validMessages.size
    }

    val lastRead = read.lastRead
        ?: throw IllegalStateException("ChannelUserRead instance for ${read.user.id} missing lastRead date")

    if (validMessages.size <= 1) {
        logger.logD("Zero or one message; returning ChannelUserRead::unreadMessages (${read.unreadMessages})")
        return read.unreadMessages
    }

    return validMessages.count { message ->
        val messageCreatedAt =
            message.createdAt
                ?: message.createdLocallyAt
                ?: throw IllegalStateException("Message ${message.id} missing creation date")

        lastRead
            .before(messageCreatedAt)
            .also { unread ->
                logger.logD("Last read @ [$lastRead] is ${if (unread) "before" else "the same or older than"} [$messageCreatedAt]")
            }
    }
        .let { count ->
            logger.logD("Counted $count unread messages")
            /**
             * The backend returns how many [ChannelUserRead.unreadMessages] it has calculated as of the time we've
             * requested channels. We only get one message when querying for a list of channels. That one message is the latest and,
             * if the unread count on the ChannelUserRead is greater than 0, that message is one of the unread messages. So,
             * if the unreadMessages count is greater than 0, remove 1 from the count.
             */
            val unreadFromServerFactor = if (read.unreadMessages > 0) 1 else 0
            val calculated = read.unreadMessages + count - unreadFromServerFactor
            logger.logD("Calculated $calculated unread messages (ChannelUserRead::unreadMessages (${read.unreadMessages}) + counted ($count) - unreadFromServer ($unreadFromServerFactor)")
            calculated
        }
}
