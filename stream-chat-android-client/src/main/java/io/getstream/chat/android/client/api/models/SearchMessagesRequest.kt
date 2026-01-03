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

package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySorter

public data class SearchMessagesRequest @JvmOverloads constructor(
    /**
     * Pagination offset. Cannot be used with sort or next.
     */
    val offset: Int?,

    /**
     * Number of messages to return
     */
    val limit: Int?,

    /**
     * Channel filter conditions
     */
    val channelFilter: FilterObject,

    /**
     * Message filter conditions
     */
    val messageFilter: FilterObject,

    /**
     * Pagination parameter. Cannot be used with non-zero offset.
     */
    val next: String? = null,

    /**
     * Sort parameters. Cannot be used with non-zero offset
     */
    val querySort: QuerySorter<Message>? = null,
) {
    val sort: List<Map<String, Any>>? = querySort?.toDto()
}
