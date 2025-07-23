/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.ui.common.R
import kotlin.math.min

/**
 * Returns the subtitle for the poll based on the maximum number of votes allowed.
 */
@InternalStreamChatApi
public fun Poll.getSubtitle(context: Context): String {
    if (closed) {
        return context.getString(R.string.stream_ui_poll_description_closed)
    }
    val maxVotesAllowed = min(maxVotesAllowed, options.size)
    return when {
        maxVotesAllowed == 1 -> context.getString(R.string.stream_ui_poll_description_single_answer)
        else -> context.getString(R.string.stream_ui_poll_description_multiple_answers, maxVotesAllowed)
    }
}
