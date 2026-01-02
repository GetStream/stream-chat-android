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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.model.UserPresence

public val User.initials: String
    get() = name.initials()

public fun List<User>.getUserByNameOrId(nameOrId: String): User? {
    return firstOrNull { it.name == nameOrId } ?: firstOrNull { it.id == nameOrId }
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
