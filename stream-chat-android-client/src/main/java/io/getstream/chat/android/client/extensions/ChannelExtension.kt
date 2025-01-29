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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.internal.containsUserMention
import io.getstream.chat.android.client.extensions.internal.wasCreatedAfter
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId

/**
 * Checks if the channel is an anonymous channel (without name).
 */
public fun Channel.isAnonymousChannel(): Boolean = id.isAnonymousChannelId()

/**
 * Checks if the channel is pinned or not for the current user.
 *
 * @return True if the channel is pinned for the current user.
 */
public fun Channel.isPinned(): Boolean = membership?.pinnedAt != null

/**
 * Checks if the channel is archived or not for the current user.
 *
 * @return True if the channel is archived for the current user.
 */
public fun Channel.isArchive(): Boolean = membership?.archivedAt != null

/**
 * Checks if [Channel] is muted for [user].
 *
 * @return True if the channel is muted for [user].
 */
public fun Channel.isMutedFor(user: User): Boolean = user.channelMutes.any { mute -> mute.channel?.cid == cid }

/**
 * Returns a list of users that are members of the channel excluding the currently
 * logged in user.
 *
 * @param currentUser The currently logged in user.
 * @return The list of users in the channel without the current user.
 */
@InternalStreamChatApi
public fun Channel.getUsersExcludingCurrent(
    currentUser: User? = ChatClient.instance().getCurrentUser(),
): List<User> = getMembersExcludingCurrent(currentUser).map { it.user }

/**
 * Returns a list of members of the channel excluding the currently logged in user.
 *
 * @param currentUser The currently logged in user.
 * @return The list of members in the channel without the current user.
 */
@InternalStreamChatApi
public fun Channel.getMembersExcludingCurrent(
    currentUser: User? = ChatClient.instance().getCurrentUser(),
): List<Member> =
    members.filter { it.user.id != currentUser?.id }

/**
 * Counts messages in which [user] is mentioned.
 * The method relies on the [Channel.messages] list and doesn't do any API call.
 * Therefore, the count might be not reliable as it relies on the local data.
 *
 * @param user The User object for which unread mentions are counted.
 *
 * @return Number of messages containing unread user mention.
 */
public fun Channel.countUnreadMentionsForUser(user: User): Int {
    val lastMessageSeenDate = read.firstOrNull { read -> read.user.id == user.id }?.lastRead

    val messagesToCheck = if (lastMessageSeenDate == null) {
        messages
    } else {
        messages.filter { message -> message.wasCreatedAfter(lastMessageSeenDate) }
    }

    return messagesToCheck.count { message -> message.containsUserMention(user) }
}

/**
 * Returns the number of unread messages in the channel for the current user.
 *
 * @return The number of unread messages in the channel for the current user.
 */
public fun Channel.currentUserUnreadCount(
    currentUserId: UserId? = ChatClient.instance().getCurrentUser()?.id,
): Int = read.firstOrNull { it.user.id == currentUserId }?.unreadMessages ?: 0

/**
 * Synchronizes the unread count of the channel with the read state of the current user.
 *
 * @param currentUserId The ID of the current user. If not provided, the ID of the current user will be taken
 * from the ChatClient if it is available.
 *
 * @return A new instance of the channel with the unread count synchronized with the read state of the current user.
 */
public fun Channel.syncUnreadCountWithReads(
    currentUserId: UserId? = ChatClient.instance().getCurrentUser()?.id,
): Channel =
    copy(unreadCount = currentUserUnreadCount(currentUserId))
