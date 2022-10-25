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
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.uiutils.extension.getMembersStatusText

internal fun Channel.isCurrentUserBanned(): Boolean {
    val currentUserId = ChatClient.instance().getCurrentUser()?.id ?: return false
    return members.any { it.user.id == currentUserId && it.banned }
}

/**
 * Returns a string describing the member status of the channel: either a member count for a group channel
 * or the last seen text for a direct one-to-one conversation with the current user.
 *
 * @param context The context to load string resources.
 * @param currentUser The currently logged in user.
 * @return The text that represent the member status of the channel.
 */
public fun Channel.getMembersStatusText(
    context: Context,
    currentUser: User? = ChatUI.currentUserProvider.getCurrentUser(),
): String {
    return getMembersStatusText(
        context = context,
        currentUser = currentUser,
        userOnlineResId = R.string.stream_ui_user_status_online,
        userLastSeenJustNowResId = R.string.stream_ui_user_status_last_seen_just_now,
        userLastSeenResId = R.string.stream_ui_user_status_last_seen,
        memberCountResId = R.plurals.stream_ui_message_list_header_member_count,
        memberCountWithOnlineResId = R.string.stream_ui_message_list_header_member_count_online,
    )
}
