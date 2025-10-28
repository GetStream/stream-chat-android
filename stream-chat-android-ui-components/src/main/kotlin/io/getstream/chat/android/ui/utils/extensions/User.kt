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

package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.uiutils.extension.getLastSeenText

/**
 * Returns a string describing the elapsed time since the user was online (was watching the channel).
 *
 * Depending on the elapsed time, the string can have one of the following formats:
 * - Online
 * - Last seen just now
 * - Last seen 13 hours ago
 *
 * @param context The context to load string resources.
 * @return A string that represents the elapsed time since the user was online.
 */
public fun User.getLastSeenText(context: Context): String = getLastSeenText(
    context = context,
    userOnlineResId = R.string.stream_ui_user_status_online,
    userLastSeenJustNowResId = R.string.stream_ui_user_status_last_seen_just_now,
    userLastSeenResId = R.string.stream_ui_user_status_last_seen,
)

internal fun User.isCurrentUser(): Boolean = id == ChatUI.currentUserProvider.getCurrentUser()?.id

internal fun User.asMention(context: Context): String? = name.takeIf(String::isNotEmpty)?.let {
    context.getString(R.string.stream_ui_mention, it)
}
