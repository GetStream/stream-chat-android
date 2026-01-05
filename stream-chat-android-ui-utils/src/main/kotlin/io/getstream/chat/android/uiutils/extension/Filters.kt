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

package io.getstream.chat.android.uiutils.extension

import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User

/**
 * Create the default channel list filter for the given user.
 *
 * @param user The currently logged in user.
 * @return The default filter for the channel list view.
 */
public fun Filters.defaultChannelListFilter(user: User?): FilterObject? {
    return if (user == null) {
        null
    } else {
        and(
            eq("type", "messaging"),
            `in`("members", listOf(user.id)),
        )
    }
}
