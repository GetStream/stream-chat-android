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

package io.getstream.chat.android.compose.ui.util

import android.content.Context
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.model.UserPresence
import io.getstream.chat.android.ui.common.utils.extensions.getMembersStatusText
import io.getstream.chat.android.ui.common.utils.extensions.getPreviewMessage
import java.util.Date

/**
 * Returns channel's last regular or system message if exists.
 * Deleted and silent messages, as well as messages from shadow-banned users, are not taken into account.
 *
 * @return Last message from the channel or null if it doesn't exist.
 */
public fun Channel.getLastMessage(currentUser: User?): Message? = getPreviewMessage(currentUser)

/**
 * Filters the read status of each person other than the target user.
 *
 * @param userToIgnore The user whose message it is.
 *
 * @return List of [Date] values that represent a read status for each other user in the channel.
 */
public fun Channel.getReadStatuses(userToIgnore: User?): List<Date> {
    return read.filter { it.user.id != userToIgnore?.id }
        .mapNotNull { it.lastRead }
}

/**
 * Checks if the channel is distinct.
 *
 * A distinct channel is a channel created without ID based on members. Internally
 * the server creates a CID which starts with "!members" prefix and is unique for
 * this particular group of users.
 *
 * @return True if the channel is distinct.
 */
public fun Channel.isDistinct(): Boolean = cid.contains("!members")

/**
 * Checks if the channel is a direct conversation between the current user and some
 * other user.
 *
 * A one-to-one chat is basically a corner case of a distinct channel with only 2 members.
 *
 * @param currentUser The currently logged in user.
 * @return True if the channel is a one-to-one conversation.
 */
public fun Channel.isOneToOne(currentUser: User?): Boolean {
    return isDistinct() &&
        members.size == 2 &&
        members.any { it.user.id == currentUser?.id }
}

/**
 * Returns a string describing the member status of the channel: either a member count for a group channel
 * or the last seen text for a direct one-to-one conversation with the current user.
 *
 * @param context The context to load string resources.
 * @param currentUser The currently logged in user.
 * @param userPresence The user presence display configuration.
 */
public fun Channel.getMembersStatusText(
    context: Context,
    currentUser: User?,
    userPresence: UserPresence = UserPresence(),
): String {
    return getMembersStatusText(
        context = context,
        currentUser = currentUser,
        countCurrentUserAsOnlineMember = userPresence.currentUser.countAsOnlineMember,
        countOtherUsersAsOnlineMembers = userPresence.otherUsers.countAsOnlineMember,
        userOnlineResId = R.string.stream_compose_user_status_online,
        userLastSeenJustNowResId = R.string.stream_compose_user_status_last_seen_just_now,
        userLastSeenResId = R.string.stream_compose_user_status_last_seen,
        memberCountResId = R.plurals.stream_compose_member_count,
        memberCountWithOnlineResId = R.string.stream_compose_member_count_online,
    )
}

/**
 * Returns a list of users that are members of the channel excluding the currently
 * logged in user.
 *
 * @param currentUser The currently logged in user.
 * @return The list of users in the channel without the current user.
 */
public fun Channel.getOtherUsers(currentUser: User?): List<User> {
    val currentUserId = currentUser?.id
    return if (currentUserId != null) {
        members.filterNot { it.user.id == currentUserId }
    } else {
        members
    }.map { it.user }
}
