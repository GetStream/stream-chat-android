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

package io.getstream.chat.android.ui.common.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Resolves how long a search query is debounced for, based on its length.
 *
 * Queries of one or two characters match a large portion of the data set, which makes them the
 * slowest ones to serve, while they are usually just a step towards the query the user is after.
 * The thresholds match the other Stream Chat SDKs.
 */
@InternalStreamChatApi
public object SearchDebounce {

    public const val SHORT_QUERY_MAX_LENGTH: Int = 2

    public const val SHORT_QUERY_DEBOUNCE_MS: Long = 500L

    /**
     * Returns the debounce period for [query], never shorter than [debounceMs].
     */
    public fun debounceMsFor(query: String, debounceMs: Long): Long = when {
        query.isEmpty() || query.length > SHORT_QUERY_MAX_LENGTH -> debounceMs
        else -> maxOf(debounceMs, SHORT_QUERY_DEBOUNCE_MS)
    }
}
