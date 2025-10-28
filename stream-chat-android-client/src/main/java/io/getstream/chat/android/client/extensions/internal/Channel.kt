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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.client.extensions.syncUnreadCountWithReads
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import java.util.Date

private const val TAG = "Chat:ChannelTools"

/**
 * Returns all users including watchers of a channel that are associated with it.
 */
@InternalStreamChatApi
public fun Channel.users(): List<User> = members.map(Member::user) +
    read.map(ChannelUserRead::user) +
    createdBy +
    messages.flatMap { it.users() } +
    watchers

/**
 * Retrieves the last not deleted [Message] of the [Channel], calculated as the messages with the most recent
 * [Message.createdAt] or [Message.createdLocallyAt].
 */
@InternalStreamChatApi
public val Channel.lastMessage: Message?
    get() = messages
        .filterNot { it.isDeleted() }
        .maxByOrNull { it.createdAt ?: it.createdLocallyAt ?: Date(0) }

/**
 * Updates the [Channel] with newest [Message].
 *
 * @param receivedEventDate Date when the event which updates the message was received.
 * @param message New message to update the channel with.
 * @param currentUserId User id of the currently logged in user.
 */
@InternalStreamChatApi
public fun Channel.updateLastMessage(
    receivedEventDate: Date,
    message: Message,
    currentUserId: String,
): Channel {
    val createdAt = message.createdAt ?: message.createdLocallyAt
    checkNotNull(createdAt) { "created at cant be null, be sure to set message.createdAt" }

    val newMessages = (
        messages
            .associateBy { it.id } + (message.id to message)
        )
        .values
        .filterNot { it.isDeleted() }
        .sortedBy { it.createdAt ?: it.createdLocallyAt }

    val newReads = read.map { read ->
        read.takeUnless { it.user.id == currentUserId }
            ?: read.copy(
                lastReceivedEventDate = receivedEventDate,
                unreadMessages = read.let {
                    val hasNewUnreadMessage = receivedEventDate.after(it.lastReceivedEventDate) &&
                        newMessages.size > messages.size &&
                        newMessages.last().id == message.id &&
                        !message.shadowed &&
                        !message.silent
                    if (hasNewUnreadMessage) it.unreadMessages.inc() else it.unreadMessages
                },
            )
    }
    return this.copy(
        messages = newMessages,
        read = newReads,
    ).syncUnreadCountWithReads(currentUserId)
}

/**
 * Removes member from the [Channel.members] and aligns [Channel.memberCount].
 *
 * @param memberUserId User id of the removed member.
 */
@InternalStreamChatApi
public fun Channel.removeMember(memberUserId: String?): Channel = copy(
    members = members.filterNot { it.user.id == memberUserId },
    memberCount = memberCount - (1.takeIf { members.any { it.user.id == memberUserId } } ?: 0),
)

/**
 * Adds member to the [Channel.members] and aligns [Channel.memberCount].
 *
 * @param member Added member.
 */
@InternalStreamChatApi
public fun Channel.addMember(member: Member): Channel {
    val memberExists = members.any { it.getUserId() == member.getUserId() }
    return copy(
        members = members + listOfNotNull(member.takeUnless { memberExists }),
        memberCount = memberCount + (1.takeUnless { memberExists } ?: 0),
    )
}

/**
 * Updates [Channel] member.
 *
 * @param member Updated member.
 */
@InternalStreamChatApi
public fun Channel.updateMember(member: Member): Channel = copy(
    members = members.map { iterableMember ->
        iterableMember.takeUnless { it.getUserId() == member.getUserId() } ?: member
    },
)

/**
 * Updates [Member.banned] property inside the [Channel.members].
 *
 * @param memberUserId Updated member user id.
 * @param banned Shows whether a user is banned or not in this channel.
 * @param shadow Shows whether a user is shadow banned or not in this channel.
 */
@InternalStreamChatApi
public fun Channel.updateMemberBanned(
    memberUserId: String,
    banned: Boolean,
    shadow: Boolean,
): Channel = copy(
    members = members.map { member ->
        member.takeUnless { it.user.id == memberUserId }
            ?: member.copy(banned = banned, shadowBanned = shadow)
    },
)

/**
 * Sets [Channel.membership] to [member] if [currentUserId] equals to [member.getUserId()].
 *
 * @param currentUserId User id of the currently logged in user.
 * @param member Added member.
 */
@InternalStreamChatApi
public fun Channel.addMembership(currentUserId: String, member: Member): Channel = copy(
    membership = member.takeIf { it.getUserId() == currentUserId } ?: membership,
)

/**
 * Sets [Channel.membership] to [member] if [member.user.id] equals to [Channel.membership.user.id].
 *
 * @param member Updated member.
 */
@InternalStreamChatApi
public fun Channel.updateMembership(member: Member): Channel = copy(
    membership = member
        .takeIf { it.getUserId() == membership?.getUserId() }
        ?: membership.also {
            StreamLog.w(TAG) {
                "[updateMembership] rejected; memberUserId(${member.getUserId()}) != " +
                    "membershipUserId(${membership?.getUserId()})"
            }
        },
)

/**
 * Sets [Channel.membership.banned] to [banned] if [memberUserId] equals to [Channel.membership.user.id].
 *
 * @param memberUserId Member user id.
 * @param banned Shows whether a user is banned or not in this channel.
 */
@InternalStreamChatApi
public fun Channel.updateMembershipBanned(memberUserId: String, banned: Boolean): Channel = copy(
    membership = membership
        ?.takeIf { it.getUserId() == memberUserId }
        ?.copy(banned = banned)
        ?: membership,
)

/**
 * Sets [Channel.membership] to null if [currentUserId] equals to [membership.user.id].
 *
 * @param currentUserId User id of the currently logged in user.
 */
@InternalStreamChatApi
public fun Channel.removeMembership(currentUserId: String?): Channel = copy(membership = membership.takeUnless { it?.user?.id == currentUserId })

/**
 * Updates the [Channel] with a new [ChannelUserRead].
 * If an existing read for the user already exists, it will be replaced with the new one. If not, the new read will be
 * added to the list of reads.
 *
 * @param newRead New read to update the channel with.
 * @param currentUserId User id of the currently logged in user.
 */
@InternalStreamChatApi
public fun Channel.updateReads(newRead: ChannelUserRead, currentUserId: UserId): Channel {
    val oldRead = read.firstOrNull { it.user.id == newRead.user.id }
    return copy(
        read = if (oldRead != null) {
            read - oldRead + newRead
        } else {
            read + newRead
        },
    ).syncUnreadCountWithReads(currentUserId)
}

/**
 * Transforms the given [Collection] of [Channel]s by applying the given [AnyChannelPaginationRequest].
 */
@InternalStreamChatApi
public fun Collection<Channel>.applyPagination(pagination: AnyChannelPaginationRequest): List<Channel> {
    val logger by taggedLogger("Chat:ChannelSort")

    return asSequence()
        .also { channelSequence ->
            logger.d {
                val ids = channelSequence.joinToString { channel -> channel.id }
                "Sorting channels: $ids"
            }
        }
        .sortedWith(pagination.sort.comparator)
        .also { channelSequence ->
            logger.d {
                val ids = channelSequence.joinToString { channel -> channel.id }
                "Sort for channels result: $ids"
            }
        }
        .drop(pagination.channelOffset)
        .take(pagination.channelLimit)
        .toList()
}

/** Updates collection of channels with more recent data of [users]. */
@InternalStreamChatApi
public fun Collection<Channel>.updateUsers(users: Map<String, User>): List<Channel> = map { it.updateUsers(users) }

/**
 * Updates a channel with more recent data of [users]. It updates messages, members, watchers, createdBy and
 * pinnedMessages of channel instance.
 */
internal fun Channel.updateUsers(users: Map<String, User>): Channel = if (users().map(User::id).any(users::containsKey)) {
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

/**
 * Updates the live locations of the channels with the provided [locations].
 */
@InternalStreamChatApi
public fun Collection<Channel>.updateLiveLocations(locations: List<Location>): Collection<Channel> = map { channel -> channel.copy(activeLiveLocations = locations.filter { it.cid == channel.cid }) }
