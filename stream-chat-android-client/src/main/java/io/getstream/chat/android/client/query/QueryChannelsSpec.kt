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
 * Note on shape: the predefined-filter fields ([predefinedFilterName],
 * [predefinedFilterValues], [predefinedSortValues]) are declared as body-level `var` properties
 * rather than primary-constructor parameters specifically to keep the primary `<init>(filter,
 * querySort)` and the synthesized `copy(filter, querySort)` JVM signatures unchanged.
 *
 * For predefined-filter queries the three fields above form the row's stable identity in the
 * offline DB and must not change once assigned. [filter] and [querySort] are the *currently
 * resolved* values for this spec instance — for predefined queries the resolved values are
 * captured by replacing the held spec instance (see `QueryChannelsMutableState.applyResolvedSpec`).
 */
public data class QueryChannelsSpec(
    val filter: FilterObject,
    val querySort: QuerySorter<Channel>,
) {
    public var cids: Set<String> = emptySet()
    public var predefinedFilterName: String? = null
    public var predefinedFilterValues: Map<String, Any>? = null
    public var predefinedSortValues: Map<String, Any>? = null

    public companion object {

        /**
         * Builds a [QueryChannelsSpec] with all fields populated in a single call.
         */
        @Suppress("LongParameterList")
        public fun create(
            filter: FilterObject,
            querySort: QuerySorter<Channel>,
            cids: Set<String> = emptySet(),
            predefinedFilterName: String? = null,
            predefinedFilterValues: Map<String, Any>? = null,
            predefinedSortValues: Map<String, Any>? = null,
        ): QueryChannelsSpec = QueryChannelsSpec(filter, querySort).apply {
            this.cids = cids
            this.predefinedFilterName = predefinedFilterName
            this.predefinedFilterValues = predefinedFilterValues
            this.predefinedSortValues = predefinedSortValues
        }
    }
}
