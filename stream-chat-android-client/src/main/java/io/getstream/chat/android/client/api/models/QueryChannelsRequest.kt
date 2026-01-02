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

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Request body class for querying channels.
 *
 * @property filter [FilterObject] conditions used by backend to filter queries response.
 * @property offset Pagination offset.
 * @property limit Number of channels to be returned by this query channels request.
 * @property querySort [QuerySorter] Sort specification for api queries.
 * @property messageLimit Number of messages in the response.
 * @property memberLimit Number of members in the response.
 */
public data class QueryChannelsRequest(
    public val filter: FilterObject,
    public var offset: Int = 0,
    public var limit: Int,
    public val querySort: QuerySorter<Channel> = QuerySortByField(),
    public var messageLimit: Int = 0,
    public var memberLimit: Int = 1,
) : ChannelRequest<QueryChannelsRequest> {

    override var state: Boolean = true
    override var watch: Boolean = true
    override var presence: Boolean = false

    /**
     * List of sort specifications.
     */
    public val sort: List<Map<String, Any>> = querySort.toDto()

    /**
     * Sets the limit of number of messages to be returned by this backend.
     *
     * @param limit Number of messages to limit.
     *
     * @return [QueryChannelsRequest] with updated limit.
     */
    public fun withMessages(limit: Int): QueryChannelsRequest {
        messageLimit = limit
        return this
    }

    /**
     * Sets the number of channels to be returned by this backend.
     *
     * @param limit Number of channels to limit.
     *
     * @return [QueryChannelsRequest] with updated limit.
     */
    public fun withLimit(limit: Int): QueryChannelsRequest {
        this.limit = limit
        return this
    }

    /**
     * Sets the offset to this request.
     *
     * @param offset The offset value to set.
     *
     * @return [QueryChannelsRequest] with updated offset.
     */
    public fun withOffset(offset: Int): QueryChannelsRequest {
        this.offset = offset
        return this
    }

    /**
     * True if this request is querying the first page, otherwise False.
     */
    public val isFirstPage: Boolean
        get() = offset == 0
}
