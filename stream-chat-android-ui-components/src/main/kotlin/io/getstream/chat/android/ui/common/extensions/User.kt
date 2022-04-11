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
import android.text.format.DateUtils
import com.getstream.sdk.chat.utils.extensions.isInLastMinute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY

public fun User.getLastSeenText(context: Context): String {
    if (online) {
        return context.getString(R.string.stream_ui_user_status_online)
    }

    return (lastActive ?: updatedAt ?: createdAt)?.let {
        if (it.isInLastMinute()) {
            context.getString(R.string.stream_ui_user_status_last_seen_just_now)
        } else {
            context.getString(
                R.string.stream_ui_user_status_last_seen,
                DateUtils.getRelativeTimeSpanString(it.time).toString()
            )
        }
    } ?: String.EMPTY
}
