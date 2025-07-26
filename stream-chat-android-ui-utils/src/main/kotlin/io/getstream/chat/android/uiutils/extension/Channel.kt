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

package io.getstream.chat.android.uiutils.extension

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isRegular
import io.getstream.chat.android.client.utils.message.isSystem
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.utils.R

/**
 * Returns channel's last regular or system message if exists.
 * Deleted and silent messages, as well as messages from shadow-banned users, are not taken into account.
 *
 * @return Last message from the channel or null if it doesn't exist.
 */
public fun Channel.getPreviewMessage(currentUser: User?): Message? =
    if (isInsideSearch) {
        cachedLatestMessages
    } else {
        messages
    }.asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filterNot { it.isDeleted() }
        .filter { it.user.id == currentUser?.id || !it.shadowed }
        .filter { it.isRegular() || it.isSystem() }
        .maxByOrNull { requireNotNull(it.createdAt ?: it.createdLocallyAt) }

/**
 * Returns the channel name if exists, or the list of member names if the channel is distinct.
 *
 * @param context The context to load string resources.
 * @param currentUser The currently logged-in user.
 * @param fallback The resource identifier of a fallback string if the [Channel] object lacks
 * information to construct a valid display name string.
 * @param maxMembers The maximum number of members used to generate a name for a distinct channel.
 *
 * @return The display name of the channel.
 */
public fun Channel.getDisplayName(
    context: Context,
    currentUser: User? = ChatClient.instance().clientState.user.value,
    @StringRes fallback: Int,
    maxMembers: Int = 2,
): String {
    return name.takeIf { it.isNotEmpty() }
        ?: nameFromMembers(context, currentUser, maxMembers)
        ?: context.getString(fallback)
}

private fun Channel.nameFromMembers(
    context: Context,
    currentUser: User?,
    maxMembers: Int,
): String? {
    val users = getUsersExcludingCurrent(currentUser)
    return when {
        users.isNotEmpty() -> {
            val usersCount = users.size
            val userNames = users
                .sortedBy(User::name)
                .take(maxMembers)
                .joinToString { it.name }
            when (usersCount <= maxMembers) {
                true -> userNames
                else -> {
                    context.getString(
                        R.string.stream_ui_channel_list_untitled_channel_plus_more,
                        userNames,
                        usersCount - maxMembers,
                    )
                }
            }
        }

        // This channel has only the current user or only one user
        members.size == 1 -> members.first().user.name

        else -> null
    }
}

/**
 * Returns a string describing the member status of the channel: either a member count for a group channel
 * or the last seen text for a direct one-to-one conversation with the current user.
 *
 * @param context The context to load string resources.
 * @param currentUser The currently logged in user.
 * @param countCurrentUserAsOnlineMember If `true`, the current user will be counted as an online member.
 * @return The text that represent the member status of the channel.
 * @param countOtherUsersAsOnlineMembers If `true`, other users will be counted as online members.
 * @param userOnlineResId The resource identifier of the string representing an online user.
 * @param userLastSeenJustNowResId The resource identifier of the string representing a user who was last seen just now.
 * @param userLastSeenResId The resource identifier of the string representing a user who was last seen.
 * @param memberCountResId The resource identifier of the string representing the member count.
 */
@Suppress("LongParameterList")
public fun Channel.getMembersStatusText(
    context: Context,
    currentUser: User?,
    countCurrentUserAsOnlineMember: Boolean = true,
    countOtherUsersAsOnlineMembers: Boolean = true,
    @StringRes userOnlineResId: Int,
    @StringRes userLastSeenJustNowResId: Int,
    @StringRes userLastSeenResId: Int,
    @PluralsRes memberCountResId: Int,
    @StringRes memberCountWithOnlineResId: Int,
): String {
    return when {
        isOneToOne(currentUser) -> {
            if (countOtherUsersAsOnlineMembers) {
                members.first { it.user.id != currentUser?.id }
                    .user
                    .getLastSeenText(
                        context = context,
                        userOnlineResId = userOnlineResId,
                        userLastSeenJustNowResId = userLastSeenJustNowResId,
                        userLastSeenResId = userLastSeenResId,
                    )
            } else {
                ""
            }
        }

        else -> {
            val memberCountString = context.resources.getQuantityString(
                memberCountResId,
                memberCount,
                memberCount,
            )

            val onlineCount = members.count { member ->
                member.user.online && when {
                    member.user.id == currentUser?.id -> countCurrentUserAsOnlineMember
                    else -> countOtherUsersAsOnlineMembers
                }
            }

            return if (onlineCount > 0) {
                context.getString(
                    memberCountWithOnlineResId,
                    memberCountString,
                    onlineCount,
                )
            } else {
                memberCountString
            }
        }
    }
}

/**
 * Checks if the channel is a direct conversation between the current user and some
 * other user.
 *
 * A one-to-one chat is basically a corner case of a distinct channel with only 2 members.
 *
 * @param currentUser The currently logged in user.
 * @return True if the channel is a one-to-one conversation.
 */
private fun Channel.isOneToOne(currentUser: User?): Boolean {
    return cid.contains("!members") &&
        members.size == 2 &&
        members.any { it.user.id == currentUser?.id }
}
