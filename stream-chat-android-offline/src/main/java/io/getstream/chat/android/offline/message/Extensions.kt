package io.getstream.chat.android.offline.message

import io.getstream.chat.android.client.models.Attachment
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
        threadParticipants +
        (pinnedBy?.let { listOf(it) } ?: emptyList())
}

internal fun Message.shouldIncrementUnreadCount(currentUserId: String): Boolean {
    return user.id != currentUserId && !silent && !shadowed
}

internal fun Message.isEphemeral(): Boolean = type == Message.TYPE_EPHEMERAL

internal fun Message.hasAttachments(): Boolean = attachments.isNotEmpty()

internal fun Message.hasPendingAttachments(): Boolean =
    attachments.any {
        it.uploadState is Attachment.UploadState.InProgress ||
            it.uploadState is Attachment.UploadState.Idle
    }
