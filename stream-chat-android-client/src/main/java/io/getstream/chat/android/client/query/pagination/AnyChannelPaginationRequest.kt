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

package io.getstream.chat.android.client.query.pagination

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter

private const val MESSAGE_LIMIT = 30
private const val CHANNEL_LIMIT = 30
private const val MEMBER_LIMIT = 30
private const val WATCHER_LIMIT = 30

public class AnyChannelPaginationRequest(public var messageLimit: Int = MESSAGE_LIMIT) {
    public var messageFilterDirection: Pagination? = null
    public var messageFilterValue: String = ""
    public var sort: QuerySorter<Channel> = QuerySortByField()

    public var channelLimit: Int = CHANNEL_LIMIT
    public var channelOffset: Int = 0

    public var memberLimit: Int = MEMBER_LIMIT
    public var memberOffset: Int = 0

    public var watcherLimit: Int = WATCHER_LIMIT
    public var watcherOffset: Int = 0
}

internal fun AnyChannelPaginationRequest.isFirstPage(): Boolean {
    return channelOffset == 0
}

public fun AnyChannelPaginationRequest.isRequestingMoreThanLastMessage(): Boolean {
    return (isFirstPage() && messageLimit > 1) || (isNotFirstPage() && messageLimit > 0)
}

internal fun AnyChannelPaginationRequest.isNotFirstPage(): Boolean = isFirstPage().not()
