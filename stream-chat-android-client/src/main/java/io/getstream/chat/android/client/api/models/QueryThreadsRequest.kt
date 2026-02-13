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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySortByField.Companion.descByName
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Query threads request.
 *
 * @property filter The filter object for the query. Supported fields:
 *  - `channel_cid` Filter by channel CID. Supported operators: `$eq`, `$in`
 *  - `channel.disabled` Filter by channel disabled status. Supported operators: `$eq`
 *  - `channel.team` Filter by channel team. Supported operators: `$eq`, `$in`
 *  - `parent_message_id` Filter by parent message ID. Supported operators: `$eq`, `$in`
 *  - `created_by_user_id` Filter by thread creator’s user ID. Supported operators: `$eq`, `$in`
 *  - `created_at` Filter by thread creation timestamp. Supported operators: `$eq`, `$gt`, `$lt`, `$gte`, `$lte`
 *  - `updated_at` Filter by thread update timestamp. Supported operators: `$eq`, `$gt`, `$lt`, `$gte`, `$lte`
 *  - `last_message_at` Filter by last message timestamp. Supported operators: `$eq`, `$gt`, `$lt`, `$gte`, `$lte`
 *
 * For more info,
 * see [Filtering and Sorting Threads](https://getstream.io/chat/docs/android/threads/#filtering-and-sorting-threads).
 *
 * @property sort The sort object for the query. Supported fields:
 *  - `active_participant_count` Number of active participants in the thread
 *  - `created_at` Thread creation timestamp
 *  - `last_message_at` Timestamp of the last message in the thread
 *  - `parent_message_id` ID of the parent message
 *  - `participant_count` Total number of participants in the thread
 *  - `reply_count` Number of replies in the thread
 *  - `updated_at` Thread last update timestamp
 *
 *  If not provided, threads will be sorted by:
 *  1. Unread status (unread threads first)
 *  2. Last message timestamp (newest first)
 *  3. Parent message ID (descending)
 *
 * For more info,
 * see [Filtering and Sorting Threads](https://getstream.io/chat/docs/android/threads/#filtering-and-sorting-threads).
 *
 * @property watch If true, all the channels corresponding to threads returned in response will be watched.
 * Defaults to true.
 * @property limit The number of threads to return. Defaults to 10.
 * @property memberLimit The number of members to request per thread. Defaults to 100.
 * @property next The next pagination token. This token can be used to fetch the next page of threads.
 * @property participantLimit The number of thread participants to request per thread. Defaults to 100.
 * @property prev The previous pagination token. This token can be used to fetch the previous page of threads.
 * @property replyLimit The number of latest replies to fetch per thread. Defaults to 2.
 */
public data class QueryThreadsRequest @JvmOverloads constructor(
    public val filter: FilterObject? = null,
    public val sort: QuerySorter<Thread> = DefaultSort,
    public val watch: Boolean = true,
    public val limit: Int = 10,
    public val memberLimit: Int = 100,
    public val next: String? = null,
    public val participantLimit: Int = 100,
    public val prev: String? = null,
    public val replyLimit: Int = 2,
) {

    public companion object {

        /**
         * The default sort order for threads:
         * 1. Unread status (unread threads first)
         * 2. Last message timestamp (newest first)
         * 3. Parent message ID (descending)
         */
        @InternalStreamChatApi
        public val DefaultSort: QuerySorter<Thread> = QuerySortByField
            .descByName<Thread>("has_unread")
            .descByName("last_message_at")
            .descByName("parent_message_id")
    }
}
