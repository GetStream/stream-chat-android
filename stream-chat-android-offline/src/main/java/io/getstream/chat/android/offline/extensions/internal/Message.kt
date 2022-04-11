/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.extensions.internal

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import java.util.Date

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

/**
 * Fills [Message.mentionedUsersIds] based on [Message.text] and [Channel.members].
 *
 * It combines the users found in the input with pre-set [Message.mentionedUsersIds], in case someone
 * is manually added as a mention. Currently only searches through the channel members for possible mentions.
 *
 * @param channel The channel whose members we can check for the mention.
 */
internal fun Message.populateMentions(channel: Channel) {
    if ('@' !in text) {
        return
    }

    val text = text.lowercase()
    val mentions = mentionedUsersIds.toMutableSet() + channel.members.mapNotNullTo(mutableListOf()) { member ->
        if (text.contains("@${member.user.name.lowercase()}")) {
            member.user.id
        } else {
            null
        }
    }

    mentionedUsersIds = mentions.toMutableList()
}

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

/**
 * Function that parses if the unread count should be increased or not.
 *
 * @param currentUserId The id of the user that the unread count should be evaluated.
 * @param lastMessageAtDate The Date of the last message the SDK is aware of. This is normally the ChannelUserRead.lastMessageSeenDate.
 * @param isChannelMuted If the channel is muted for the current user or not.
 */
internal fun Message.shouldIncrementUnreadCount(
    currentUserId: String,
    lastMessageAtDate: Date?,
    isChannelMuted: Boolean,
): Boolean {
    if (isChannelMuted) return false

    val isMoreRecent = if (createdAt != null && lastMessageAtDate != null) {
        createdAt!! > lastMessageAtDate
    } else {
        true
    }

    return user.id != currentUserId && !silent && !shadowed && isMoreRecent
}

internal fun Message.isEphemeral(): Boolean = type == Message.TYPE_EPHEMERAL

internal fun Message.hasPendingAttachments(): Boolean =
    attachments.any {
        it.uploadState is Attachment.UploadState.InProgress ||
            it.uploadState is Attachment.UploadState.Idle
    }
