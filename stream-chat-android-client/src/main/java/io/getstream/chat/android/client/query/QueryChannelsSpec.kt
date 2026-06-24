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

package io.getstream.chat.android.client.query

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Spec describing a query channels operation and the channel CIDs that belong to it.
 *
 * Three identity flavors are supported:
 *  - Standard queries: identity is `(filter, querySort)`.
 *  - Predefined queries: identity is [predefinedFilterName] plus the interpolation value maps. The
 *    [filter] and [querySort] held by this spec instance are the *currently resolved* values for
 *    that predefined query (captured by replacing the held spec instance — see
 *    `QueryChannelsMutableState.applyResolvedSpec`).
 *  - Grouped queries: identity is [groupKey], the stable key returned by the server's grouped
 *    channels endpoint. [filter] and [querySort] hold neutral placeholders for these queries.
 *
 * The 2-arg and 6-arg [constructor]s plus their matching [copy] overloads are kept for binary
 * compatibility with callers that predate the variant-specific fields. They delegate to the
 * primary constructor with the extras defaulted to their empty/null values.
 */
public data class QueryChannelsSpec(
    val filter: FilterObject,
    val querySort: QuerySorter<Channel>,
    var cids: Set<String> = emptySet(),
    val predefinedFilterName: String? = null,
    val predefinedFilterValues: Map<String, Any>? = null,
    val predefinedSortValues: Map<String, Any>? = null,
    val groupKey: String? = null,
) {
    public constructor(
        filter: FilterObject,
        querySort: QuerySorter<Channel>,
    ) : this(filter, querySort, emptySet(), null, null, null, null)

    public constructor(
        filter: FilterObject,
        querySort: QuerySorter<Channel>,
        cids: Set<String>,
        predefinedFilterName: String?,
        predefinedFilterValues: Map<String, Any>?,
        predefinedSortValues: Map<String, Any>?,
    ) : this(filter, querySort, cids, predefinedFilterName, predefinedFilterValues, predefinedSortValues, null)

    public fun copy(
        filter: FilterObject = this.filter,
        querySort: QuerySorter<Channel> = this.querySort,
    ): QueryChannelsSpec = QueryChannelsSpec(
        filter = filter,
        querySort = querySort,
        cids = cids,
        predefinedFilterName = predefinedFilterName,
        predefinedFilterValues = predefinedFilterValues,
        predefinedSortValues = predefinedSortValues,
        groupKey = groupKey,
    )

    public fun copy(
        filter: FilterObject = this.filter,
        querySort: QuerySorter<Channel> = this.querySort,
        cids: Set<String> = this.cids,
        predefinedFilterName: String? = this.predefinedFilterName,
        predefinedFilterValues: Map<String, Any>? = this.predefinedFilterValues,
        predefinedSortValues: Map<String, Any>? = this.predefinedSortValues,
    ): QueryChannelsSpec = QueryChannelsSpec(
        filter = filter,
        querySort = querySort,
        cids = cids,
        predefinedFilterName = predefinedFilterName,
        predefinedFilterValues = predefinedFilterValues,
        predefinedSortValues = predefinedSortValues,
        groupKey = groupKey,
    )
}
