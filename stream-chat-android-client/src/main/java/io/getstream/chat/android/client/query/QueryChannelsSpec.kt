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
 * Immutable identity of a channels query.
 *
 * @property filter Filter conditions for the query.
 * @property querySort Sort specification for the query.
 * @property cids CIDs of channels currently associated with this query.
 * @property groupKey Non-null for grouped queries; identifies the group across reconnects.
 */
public data class QueryChannelsSpec(
    val filter: FilterObject,
    val querySort: QuerySorter<Channel>,
    var cids: Set<String> = emptySet(),
    val groupKey: String? = null,
) {

    /**
     * Two-argument constructor preserved for source and binary compatibility with prior versions
     * of this class (when [cids] and [groupKey] were mutable body fields).
     */
    public constructor(
        filter: FilterObject,
        querySort: QuerySorter<Channel>,
    ) : this(filter, querySort, emptySet(), null)

    /**
     * Two-argument [copy] preserved for binary compatibility. Existing bytecode that referenced the
     * pre-refactor 2-arg `copy(filter, querySort)` continues to resolve through this method.
     * [cids] and [groupKey] are carried over from the receiver.
     *
     * Source callers using `spec.copy(filter = x)` resolve here (more applicable overload than the
     * auto-generated 4-arg copy), so cids/groupKey are preserved automatically.
     */
    public fun copy(
        filter: FilterObject = this.filter,
        querySort: QuerySorter<Channel> = this.querySort,
    ): QueryChannelsSpec = QueryChannelsSpec(filter, querySort, cids, groupKey)
}
