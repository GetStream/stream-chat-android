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

import io.getstream.chat.android.client.extensions.getCreatedAtOrNull
import io.getstream.chat.android.client.utils.message.MessageSortComparator
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * Updates the given Thread with the new message (parent or reply).
 */
@InternalStreamChatApi
public fun Thread.updateParentOrReply(message: Message): Thread {
    return when (this.parentMessageId) {
        message.id -> updateParent(parent = message)
        message.parentId -> upsertReply(reply = message)
        else -> this
    }
}

/**
 * Updates the parent message of a Thread.
 */
@InternalStreamChatApi
public fun Thread.updateParent(parent: Message): Thread {
    // Skip update if [parent] is not related to this Thread
    if (this.parentMessageId != parent.id) return this
    // Enrich the poll as it might not be present in the event
    val poll = parent.poll ?: this.parentMessage.poll
    val updatedParent = parent.copy(poll = poll)
    return this.copy(
        parentMessage = updatedParent,
        deletedAt = parent.deletedAt,
        updatedAt = parent.updatedAt ?: this.updatedAt,
    )
}

/**
 * Inserts a new reply (or updates and existing one) in a Thread.
 */
@InternalStreamChatApi
public fun Thread.upsertReply(reply: Message): Thread {
    // Ship update if [reply] is not related to this Thread
    if (this.parentMessageId != reply.parentId) return this
    val newReplies = upsertMessageInList(reply, this.latestReplies)
    val isInsert = newReplies.size > this.latestReplies.size
    val sortedNewReplies = newReplies.sortedWith(MessageSortComparator)
    val lastMessageAt = sortedNewReplies.lastOrNull()?.getCreatedAtOrNull()
    // The new message could be from a new thread participant
    val threadParticipants = if (isInsert) {
        upsertThreadParticipantInList(
            newParticipant = ThreadParticipant(user = reply.user),
            participants = this.threadParticipants,
        )
    } else {
        this.threadParticipants
    }
    val participantCount = threadParticipants.size
    // Update read counts (+1 for each non-sender of the message)
    val read = if (isInsert) {
        updateReadCounts(this.read, reply)
    } else {
        this.read
    }
    return this.copy(
        lastMessageAt = lastMessageAt ?: this.lastMessageAt,
        updatedAt = lastMessageAt ?: this.updatedAt,
        participantCount = participantCount,
        threadParticipants = threadParticipants,
        latestReplies = sortedNewReplies,
        read = read,
    )
}

/**
 * Marks the given thread as read by the given user.
 *
 * @param threadInfo The [ThreadInfo] holding info about the [Thread] which should be marked as read.
 * @param user The [User] for which the thread should be marked as read.
 * @param createdAt The [Date] of the 'mark read' event.
 */
@InternalStreamChatApi
public fun Thread.markAsReadByUser(threadInfo: ThreadInfo, user: User, createdAt: Date): Thread {
    // Skip update if [threadInfo] is not related to this Thread
    if (this.parentMessageId != threadInfo.parentMessageId) return this
    val updatedRead = this.read.map { read ->
        if (read.user.id == user.id) {
            read.copy(
                user = user,
                unreadMessages = 0,
                lastReceivedEventDate = createdAt,
            )
        } else {
            read
        }
    }
    return this.copy(
        activeParticipantCount = threadInfo.activeParticipantCount,
        deletedAt = threadInfo.deletedAt,
        lastMessageAt = threadInfo.lastMessageAt ?: this.lastMessageAt,
        parentMessage = threadInfo.parentMessage ?: this.parentMessage,
        participantCount = threadInfo.participantCount,
        title = threadInfo.title,
        updatedAt = threadInfo.updatedAt,
        read = updatedRead,
    )
}

/**
 * Marks the given thread as unread by the given user.
 *
 * @param user The [User] for which the thread should be marked as read.
 * @param createdAt The [Date] of the 'mark read' event.
 */
@InternalStreamChatApi
public fun Thread.markAsUnreadByUser(user: User, createdAt: Date): Thread {
    val updatedRead = this.read.map { read ->
        if (read.user.id == user.id) {
            read.copy(
                user = user,
                // Update this value to what the backend returns (when implemented)
                unreadMessages = read.unreadMessages + 1,
                lastReceivedEventDate = createdAt,
            )
        } else {
            read
        }
    }
    return this.copy(read = updatedRead)
}

private fun upsertMessageInList(newMessage: Message, messages: List<Message>): List<Message> {
    // Insert
    if (messages.none { it.id == newMessage.id }) {
        return messages + listOf(newMessage)
    }
    // Update
    return messages.map { message ->
        if (message.id == newMessage.id) {
            newMessage
        } else {
            message
        }
    }
}

private fun upsertThreadParticipantInList(
    newParticipant: ThreadParticipant,
    participants: List<ThreadParticipant>,
): List<ThreadParticipant> {
    // Insert
    if (participants.none { it.getUserId() == newParticipant.getUserId() }) {
        return participants + listOf(newParticipant)
    }
    // Update
    return participants.map { participant ->
        if (participant.getUserId() == newParticipant.getUserId()) {
            newParticipant
        } else {
            participant
        }
    }
}

private fun updateReadCounts(read: List<ChannelUserRead>, reply: Message): List<ChannelUserRead> {
    return read.map { userRead ->
        if (userRead.user.id != reply.user.id) {
            userRead.copy(unreadMessages = userRead.unreadMessages + 1)
        } else {
            userRead
        }
    }
}
