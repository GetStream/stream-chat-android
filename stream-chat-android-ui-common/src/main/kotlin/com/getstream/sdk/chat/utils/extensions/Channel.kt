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

package com.getstream.sdk.chat.utils.extensions

import android.content.Context
import androidx.annotation.StringRes
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun Channel.isDirectMessaging(): Boolean {
    return members.size == 2 && includesCurrentUser()
}

private fun Channel.includesCurrentUser(): Boolean {
    val currentUserId = ChatClient.instance().getCurrentUser()?.id ?: return false
    return members.any { it.user.id == currentUserId }
}

/**
 * Returns the channel name if exists, or the list of member names if the channel is distinct.
 *
 * @param context The context to load string resources.
 * @param currentUser The currently logged-in user.
 * @param fallback The resource identifier of a fallback string if the [Channel] object lacks
 * @param maxMembers The maximum number of members used to generate a name for a distinct channel.
 * information to construct a valid display name string.
 *
 * @return The display name of the channel.
 */
@InternalStreamChatApi
public fun Channel.getDisplayName(
    context: Context,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    @StringRes fallback: Int,
    maxMembers: Int = 5,
): String {
    return name.takeIf { it.isNotEmpty() }
        ?: nameFromMembers(currentUser, maxMembers)
        ?: context.getString(fallback)
}

private fun Channel.nameFromMembers(currentUser: User?, maxMembers: Int): String? {
    val users = getUsersExcludingCurrent(currentUser)

    return when {
        users.isNotEmpty() -> users.joinToString(limit = maxMembers, transform = { it.name }).takeIf { it.isNotEmpty() }

        // This channel has only the current user or only one user
        members.size == 1 -> members.first().user.name

        else -> null
    }
}
