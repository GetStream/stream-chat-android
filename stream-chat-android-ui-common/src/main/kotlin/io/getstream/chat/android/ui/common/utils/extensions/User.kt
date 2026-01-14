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

package io.getstream.chat.android.ui.common.utils.extensions

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.model.UserPresence
import java.util.Date

/**
 * Returns the initials of the user.
 */
public val User.initials: String
    get() = name.initials()

/**
 * Finds a user in the list by their name or id.
 */
public fun List<User>.getUserByNameOrId(nameOrId: String): User? {
    return firstOrNull { it.name == nameOrId } ?: firstOrNull { it.id == nameOrId }
}

/**
 * Returns a string describing the elapsed time since the user was online (was watching the channel).
 *
 * Depending on the elapsed time, the string can have one of the following formats:
 * - Online
 * - Last seen just now
 * - Last seen 13 hours ago
 *
 * @param context The context to load string resources.
 * @param userOnlineResId Resource id for the online text.
 * @param userLastSeenJustNowResId Resource id for the just now text.
 * @param userLastSeenResId Resource id for the last seen text.
 * @return A string that represents the elapsed time since the user was online.
 */
public fun User.getLastSeenText(
    context: Context,
    @StringRes userOnlineResId: Int,
    @StringRes userLastSeenJustNowResId: Int,
    @StringRes userLastSeenResId: Int,
): String {
    if (online) {
        return context.getString(userOnlineResId)
    }

    return (lastActive ?: updatedAt ?: createdAt)?.let {
        if (it.isInLastMinute()) {
            context.getString(userLastSeenJustNowResId)
        } else {
            context.getString(
                userLastSeenResId,
                DateUtils.getRelativeTimeSpanString(it.time).toString(),
            )
        }
    } ?: ""
}

/**
 * Determines if the online indicator should be shown for the user based on the user presence configuration.
 *
 * @param userPresence The user presence configuration.
 * @param currentUser The current user.
 */
@InternalStreamChatApi
public fun User.shouldShowOnlineIndicator(
    userPresence: UserPresence,
    currentUser: User?,
): Boolean =
    when {
        id == currentUser?.id -> userPresence.currentUser.showOnlineIndicator
        else -> userPresence.otherUsers.showOnlineIndicator
    }

private fun Date.isInLastMinute(): Boolean = (Date().time - ONE_MINUTE_IN_MILLIS < time)

private const val ONE_MINUTE_IN_MILLIS = 60000
