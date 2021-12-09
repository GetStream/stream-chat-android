package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.message.users

/** Updates collection of messages with more recent data of [users]. */
internal fun Collection<Message>.updateUsers(users: Map<String, User>) = map { it.updateUsers(users) }

/**
 * Updates a message with more recent data of [users]. It updates author user, latestReactions, replyTo message,
 * mentionedUsers, threadParticipants and pinnedBy user of this instance.
 */
internal fun Message.updateUsers(users: Map<String, User>): Message =
    if (users().map(User::id).any(users::containsKey)) {
        copy(
            user = if (users.containsKey(user.id)) {
                users[user.id] ?: user
            } else user,
            latestReactions = latestReactions.updateByUsers(users).toMutableList(),
            replyTo = replyTo?.updateUsers(users),
            mentionedUsers = mentionedUsers.updateUsers(users).toMutableList(),
            threadParticipants = threadParticipants.updateUsers(users).toMutableList(),
            pinnedBy = users[pinnedBy?.id ?: ""] ?: pinnedBy,
        )
    } else {
        this
    }
