package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import java.util.Date

internal val NEVER = Date(0)

internal fun Message.wasCreatedAfterOrAt(date: Date?): Boolean {
    return createdAt ?: createdLocallyAt ?: NEVER >= date
}

internal fun Message.wasCreatedAfter(date: Date?): Boolean {
    return createdAt ?: createdLocallyAt ?: NEVER > date
}

internal fun Message.wasCreatedBefore(date: Date?): Boolean {
    return createdAt ?: createdLocallyAt ?: NEVER < date
}

internal fun Message.wasCreatedBeforeOrAt(date: Date?): Boolean {
    return createdAt ?: createdLocallyAt ?: NEVER <= date
}

internal fun Message.users(): List<User> {
    return latestReactions.mapNotNull(Reaction::user) +
        user +
        (replyTo?.users().orEmpty()) +
        mentionedUsers +
        ownReactions.mapNotNull(Reaction::user) +
        threadParticipants
}

internal fun Message.shouldIncrementUnreadCount(currentUserId: String): Boolean {
    return user.id != currentUserId && !silent && !shadowed
}
