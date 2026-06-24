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

package io.getstream.chat.android.compose.ui.components.avatar

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User

/**
 * The presence indicator drawn on top of an avatar.
 *
 * The state is chosen per call site so each screen can decide what to show, matching the design
 * system's avatar behavior.
 */
public enum class AvatarPresenceIndicator {
    /** A green dot. Use when the user is online. */
    Online,

    /** A grey dot. Use when presence is known but the user is not currently active. */
    Offline,

    /** No indicator. Use when presence is unknown, irrelevant, or intentionally hidden. */
    None,
}

/**
 * Resolves the [AvatarPresenceIndicator] for this user.
 *
 * @param showWhenOffline Whether to show the grey [AvatarPresenceIndicator.Offline] dot when the user is offline.
 * When `false` (the default), an offline user resolves to [AvatarPresenceIndicator.None] and no dot is drawn.
 */
internal fun User.avatarPresenceIndicator(showWhenOffline: Boolean = false): AvatarPresenceIndicator =
    when {
        online -> AvatarPresenceIndicator.Online
        showWhenOffline -> AvatarPresenceIndicator.Offline
        else -> AvatarPresenceIndicator.None
    }

/**
 * Resolves the [AvatarPresenceIndicator] for this channel.
 *
 * The channel is considered online when at least one member other than [currentUser] is online.
 *
 * @param currentUser The user currently logged in, excluded when determining the channel's online state.
 * @param showWhenOffline Whether to show the grey [AvatarPresenceIndicator.Offline] dot when the channel is offline.
 * When `false` (the default), an offline channel resolves to [AvatarPresenceIndicator.None] and no dot is drawn.
 */
internal fun Channel.avatarPresenceIndicator(
    currentUser: User?,
    showWhenOffline: Boolean = false,
): AvatarPresenceIndicator {
    val online = members.any { it.user.id != currentUser?.id && it.user.online }
    return when {
        online -> AvatarPresenceIndicator.Online
        showWhenOffline -> AvatarPresenceIndicator.Offline
        else -> AvatarPresenceIndicator.None
    }
}
