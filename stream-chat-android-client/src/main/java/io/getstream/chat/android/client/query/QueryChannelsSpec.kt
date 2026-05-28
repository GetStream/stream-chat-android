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
 * For predefined-filter queries the [predefinedFilterName] plus value maps form the spec's stable
 * identity in the offline DB and must not change once assigned. [filter] and [querySort] are the
 * *currently resolved* values for this spec instance — for predefined queries the resolved values
 * are captured by replacing the held spec instance (see
 * `QueryChannelsMutableState.applyResolvedSpec`).
 *
 * The 2-arg [constructor] and 2-arg [copy] are kept for binary compatibility with callers that
 * predate the predefined-filter fields. They delegate to the primary constructor with the
 * predefined fields defaulted to their empty/null values.
 */
public data class QueryChannelsSpec(
    val filter: FilterObject,
    val querySort: QuerySorter<Channel>,
    var cids: Set<String> = emptySet(),
    val predefinedFilterName: String? = null,
    val predefinedFilterValues: Map<String, Any>? = null,
    val predefinedSortValues: Map<String, Any>? = null,
) {
    public constructor(
        filter: FilterObject,
        querySort: QuerySorter<Channel>,
    ) : this(filter, querySort, emptySet(), null, null, null)

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
    )
}
