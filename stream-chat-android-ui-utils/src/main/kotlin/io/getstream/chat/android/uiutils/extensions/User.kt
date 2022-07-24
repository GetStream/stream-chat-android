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

package io.getstream.chat.android.uiutils.extensions

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import io.getstream.chat.android.client.models.User

/**
 * Returns a string describing the elapsed time since the user was online (was watching the channel).
 *
 * Depending on the elapsed time, the string can have one of the following formats:
 * - Online
 * - Last seen just now
 * - Last seen 13 hours ago
 *
 * @param context The context to load string resources.
 * @param onlineResId Resource id for the online text.
 * @param justNowResId Resource id for the just now text.
 * @param lastSeenResId Resource id for the last seen text.
 * @return A string that represents the elapsed time since the user was online.
 */
public fun User.getLastSeenText(
    context: Context,
    @StringRes onlineResId: Int,
    @StringRes justNowResId: Int,
    @StringRes lastSeenResId: Int,
): String {
    if (online) {
        return context.getString(onlineResId)
    }

    return (lastActive ?: updatedAt ?: createdAt)?.let {
        if (it.isInLastMinute()) {
            context.getString(justNowResId)
        } else {
            context.getString(
                lastSeenResId,
                DateUtils.getRelativeTimeSpanString(it.time).toString()
            )
        }
    } ?: ""
}
