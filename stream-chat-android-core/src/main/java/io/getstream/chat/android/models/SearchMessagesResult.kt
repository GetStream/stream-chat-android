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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Model representing a result of messages search operation.
 */
@Immutable
public data class SearchMessagesResult(
    /**
     * Search results
     */
    val messages: List<Message> = emptyList(),

    /**
     * Value to pass to the next search query in order to paginate
     */
    val next: String? = null,

    /**
     * Value that points to the previous page. Pass as the next value in a search query
     * to paginate backwards.
     */
    val previous: String? = null,

    /**
     * Warning about the search results
     */
    val resultsWarning: SearchWarning? = null,
)
