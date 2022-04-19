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

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import java.util.Date

/**
 * Returns all users including watchers of a channel that are associated with it.
 */
internal fun Channel.users(): List<User> {
    return members.map(Member::user) +
        read.map(ChannelUserRead::user) +
        createdBy +
        messages.flatMap { it.users() } +
        watchers
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

/**
 * Removes member from the [Channel.members] and aligns [Channel.memberCount].
 *
 * @param userId User id of the removed member.
 */
internal fun Channel.removeMember(userId: String?): Channel {
    val member = members.find { it.user.id == userId } ?: return this
    members = members - member
    memberCount -= 1
    return this
}

/**
 * Adds member to the [Channel.members] and aligns [Channel.memberCount].
 *
 * @param member Added member.
 */
internal fun Channel.addMember(member: Member?): Channel {
    member ?: return this
    members = members + member
    memberCount += 1
    return this
}

/**
 * Updates member to the [Channel.members].
 *
 * @param member Updated member.
 */
internal fun Channel.updateMember(member: Member?): Channel {
    val userId = member?.user?.id ?: return this
    members = members.filterNot { it.user.id == userId } + member
    return this
}

/**
 * Sets [Channel.membership] to null if [currentUser] equals to [Member.user].
 *
 * @param member Added member.
 * @param currentUserId User id of the currently logged in user.
 */
internal fun Channel.addMembership(member: Member?, currentUserId: String?): Channel {
    if (member?.user?.id == currentUserId) {
        membership = member
    }
    return this
}

/**
 * Sets [Channel.membership] to null if [currentUser] equals to [Member.user].
 *
 * @param currentUserId User id of the currently logged in user.
 */
internal fun Channel.removeMembership(currentUserId: String?): Channel {
    if (membership?.user?.id == currentUserId) {
        membership = null
    }
    return this
}

internal fun Channel.updateReads(newRead: ChannelUserRead) {
    val oldRead = read.firstOrNull { it.user == newRead.user }
    read = if (oldRead != null) {
        read - oldRead + newRead
    } else {
        read + newRead
    }
}

/**
 * Increments channel's unread for the specific user.
 *
 * @param currentUserId The id of the user that should have the unread count incremented for this Channel.
 * @param lastMessageSeenDate The Date of the last message that the SDK is aware of.
 */
internal fun Channel.incrementUnreadCount(currentUserId: String, lastMessageSeenDate: Date?) {
    read.firstOrNull { it.user.id == currentUserId }
        ?.let {
            it.lastMessageSeenDate = lastMessageSeenDate
            it.unreadMessages++
        }
}

internal fun Collection<Channel>.applyPagination(pagination: AnyChannelPaginationRequest): List<Channel> {
    return asSequence().sortedWith(pagination.sort.comparator)
        .drop(pagination.channelOffset)
        .take(pagination.channelLimit)
        .toList()
}

/** Updates collection of channels with more recent data of [users]. */
internal fun Collection<Channel>.updateUsers(users: Map<String, User>) = map { it.updateUsers(users) }

/**
 * Updates a channel with more recent data of [users]. It updates messages, members, watchers, createdBy and
 * pinnedMessages of channel instance.
 */
internal fun Channel.updateUsers(users: Map<String, User>): Channel {
    return if (users().map(User::id).any(users::containsKey)) {
        copy(
            messages = messages.updateUsers(users),
            members = members.updateUsers(users).toList(),
            watchers = watchers.updateUsers(users),
            createdBy = users[createdBy.id] ?: createdBy,
            pinnedMessages = pinnedMessages.updateUsers(users),
        )
    } else {
        this
    }
}
