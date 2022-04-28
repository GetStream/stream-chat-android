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

package io.getstream.chat.android.ui.common.extensions

import android.content.Context
import androidx.annotation.StringRes
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.isCurrentUser

/**
 * Returns the channel name if exists, or the list of member names if the channel is distinct.
 *
 * @param context The context to load string resources.
 * @param currentUser The currently logged-in user.
 * @param fallback The resource identifier of a fallback string if the [Channel] object lacks
 * information to construct a valid display name string.
 *
 * @return The display name of the channel.
 */
@InternalStreamChatApi
@JvmOverloads
public fun Channel.getDisplayName(
    context: Context,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    @StringRes fallback: Int = R.string.stream_ui_channel_list_untitled_channel,
): String {
    return name.takeIf { it.isNotEmpty() }
        ?: nameFromMembers(currentUser)
        ?: context.getString(fallback)
}

private fun Channel.nameFromMembers(currentUser: User?): String? {
    val users = getUsersExcludingCurrent(currentUser)

    return when {
        users.isNotEmpty() -> users.joinToString { it.name }.takeIf { it.isNotEmpty() }

        // This channel has only the current user or only one user
        members.size == 1 -> members.first().user.name

        else -> null
    }
}

/**
 * Returns channel's last regular or system message if exists.
 * Deleted and silent messages, as well as messages from shadow-banned users, are not taken into account.
 *
 * @return Last message from the channel or null if it doesn't exist.
 */
public fun Channel.getLastMessage(): Message? =
    messages.asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filter { it.deletedAt == null }
        .filter { !it.silent }
        .filter { it.user.isCurrentUser() || !it.shadowed }
        .filter { it.isRegular() || it.isSystem() }
        .maxByOrNull { it.getCreatedAtOrThrow() }
