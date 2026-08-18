/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * Replaces the [Message.member] snapshot of this message, keeping the deprecated [Message.channelRole] in sync with it.
 *
 * The snapshot is taken verbatim: the same value reaches the in-memory state, the repository cache and the database, so
 * the three cannot disagree. A blanket column update cannot preserve a previously known role, so neither does this.
 */
@InternalStreamChatApi
@Suppress("DEPRECATION")
public fun Message.withMemberInfo(memberInfo: MemberInfo?): Message =
    copy(member = memberInfo, channelRole = memberInfo?.channelRole)

/**
 * Whether this message, or the quoted message it carries, holds an out of date [Message.member] snapshot for [userId].
 */
@InternalStreamChatApi
public fun Message.hasOutdatedMemberInfo(userId: String, memberInfo: MemberInfo?): Boolean =
    hasOutdatedMemberInfoFor(userId, memberInfo) || replyTo?.hasOutdatedMemberInfoFor(userId, memberInfo) == true

/**
 * Applies [memberInfo] to this message and to the quoted message it carries, whichever of the two [userId] authored.
 *
 * The quoted copy is a snapshot of its own, so leaving it behind would show two different snapshots for one author.
 */
@InternalStreamChatApi
public fun Message.withRefreshedMemberInfo(userId: String, memberInfo: MemberInfo?): Message {
    val refreshed = if (user.id == userId) withMemberInfo(memberInfo) else this
    val quoted = refreshed.replyTo
    return when {
        quoted == null || quoted.user.id != userId -> refreshed
        else -> refreshed.copy(replyTo = quoted.withMemberInfo(memberInfo))
    }
}

private fun Message.hasOutdatedMemberInfoFor(userId: String, memberInfo: MemberInfo?): Boolean =
    user.id == userId && member != memberInfo

/** Updates collection of messages with more recent data of [users]. */
@InternalStreamChatApi
public fun Collection<Message>.updateUsers(users: Map<String, User>): List<Message> = map { it.updateUsers(users) }

/** Updates collection of messages with more recent data of [users]. */
@InternalStreamChatApi
public fun Map<String, Message>.updateUsers(users: Map<String, User>): Map<String, Message> = mapValues { (_, value) ->
    value.updateUsers(users)
}

/**
 * Updates a message with more recent data of [users]. It updates author user, latestReactions, replyTo message,
 * mentionedUsers, threadParticipants and pinnedBy user of this instance.
 */
@InternalStreamChatApi
public fun Message.updateUsers(users: Map<String, User>): Message =
    if (users().map(User::id).any(users::containsKey)) {
        copy(
            user = if (users.containsKey(user.id)) {
                users[user.id] ?: user
            } else {
                user
            },
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
@InternalStreamChatApi
public fun Message.populateMentions(channel: Channel): Message {
    if ('@' !in text) {
        return this
    }
    val text = text.lowercase()
    val mentions = mentionedUsersIds.toMutableSet() + channel.members.mapNotNullTo(mutableListOf()) { member ->
        if (text.contains("@${member.user.name.lowercase()}")) {
            member.user.id
        } else {
            null
        }
    }
    return copy(mentionedUsersIds = mentions.toList())
}

/**
 * Internal indicator of a 'never' date.
 */
@InternalStreamChatApi
public val NEVER: Date = Date(0)

/**
 * Checks if the message was created after or at the given [date].
 */
@InternalStreamChatApi
public fun Message.wasCreatedAfterOrAt(date: Date?): Boolean {
    return getCreatedAtOrDefault(NEVER) >= date
}

/**
 * Checks if the message was created after the given [date].
 */
@InternalStreamChatApi
public fun Message.wasCreatedAfter(date: Date?): Boolean {
    return getCreatedAtOrDefault(NEVER) > date
}

/**
 * Checks if the message was created before the given [date].
 */
@InternalStreamChatApi
public fun Message.wasCreatedBefore(date: Date?): Boolean {
    return getCreatedAtOrDefault(NEVER) < date
}

/**
 * Checks if the message was created before or at the given [date].
 */
@InternalStreamChatApi
public fun Message.wasCreatedBeforeOrAt(date: Date?): Boolean {
    return getCreatedAtOrDefault(NEVER) <= date
}

/**
 * Retrieves all [User]s involved in the message.
 * Includes the author, reaction authors, original message author (if the message is reply), mentioned users,
 * thread participants, pinned by user, and poll voters.
 */
@InternalStreamChatApi
public fun Message.users(): List<User> {
    return latestReactions.mapNotNull(Reaction::user) +
        user +
        (replyTo?.users().orEmpty()) +
        mentionedUsers +
        ownReactions.mapNotNull(Reaction::user) +
        threadParticipants +
        (pinnedBy?.let { listOf(it) } ?: emptyList()) +
        (poll?.votes?.mapNotNull { it.user } ?: emptyList())
}

/**
 * Function that parses if the unread count should be increased or not.
 *
 * @param currentUserId The id of the user that the unread count should be evaluated.
 * @param lastMessageAtDate The Date of the last message the SDK is aware of. This is normally the ChannelUserRead.lastMessageSeenDate.
 * @param isChannelMuted If the channel is muted for the current user or not.
 */
@InternalStreamChatApi
public fun Message.shouldIncrementUnreadCount(
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

/**
 * Checks if the given [Message] has pending attachments.
 * A pending attachment is an attachment that is either in [Attachment.UploadState.InProgress] or
 * [Attachment.UploadState.Idle].
 */
@InternalStreamChatApi
public fun Message.hasPendingAttachments(): Boolean =
    attachments.any {
        it.uploadState is Attachment.UploadState.InProgress ||
            it.uploadState is Attachment.UploadState.Idle
    }

/**
 * Checks if the message mentions the [user].
 */
internal fun Message.containsUserMention(user: User): Boolean {
    return mentionedUsersIds.contains(user.id) || mentionedUsers.any { mentionedUser -> mentionedUser.id == user.id }
}
