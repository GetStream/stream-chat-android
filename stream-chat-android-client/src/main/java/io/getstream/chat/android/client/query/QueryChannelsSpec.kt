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
 * [cids] is intentionally a body `var` and not part of the primary constructor, so it does not
 * participate in [equals]/[hashCode]/auto-generated `copy()` — it is treated as mutable spec
 * payload rather than identity.
 *
 * The 2-arg [constructor] and 2-arg [copy] are kept for binary compatibility with callers that
 * predate the variant-specific fields. They delegate to the primary constructor with the extras
 * defaulted to their empty/null values.
 */
public data class QueryChannelsSpec(
    val filter: FilterObject,
    val querySort: QuerySorter<Channel>,
    val groupKey: String? = null,
    val predefinedFilterName: String? = null,
    val predefinedFilterValues: Map<String, Any>? = null,
    val predefinedSortValues: Map<String, Any>? = null,
) {

    /**
     * CIDs of channels currently associated with this query.
     */
    var cids: Set<String> = emptySet()

    public constructor(
        filter: FilterObject,
        querySort: QuerySorter<Channel>,
    ) : this(filter, querySort, null, null, null, null)

    /**
     * Two-argument [copy] preserved for binary compatibility. Existing bytecode that referenced the
     * pre-refactor 2-arg `copy(filter, querySort)` continues to resolve through this method.
     * Variant-specific fields ([groupKey], [predefinedFilterName], [predefinedFilterValues],
     * [predefinedSortValues]) and [cids] are carried over from the receiver.
     */
    public fun copy(
        filter: FilterObject = this.filter,
        querySort: QuerySorter<Channel> = this.querySort,
    ): QueryChannelsSpec = QueryChannelsSpec(
        filter = filter,
        querySort = querySort,
        groupKey = groupKey,
        predefinedFilterName = predefinedFilterName,
        predefinedFilterValues = predefinedFilterValues,
        predefinedSortValues = predefinedSortValues,
    ).also { it.cids = this.cids }
}
