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

package io.getstream.chat.android.client.internal.state.plugin

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Identifies a query channels operation independently of the resolved [FilterObject] and
 * [QuerySorter]. Used as the cache key in `StateRegistry`, `LogicRegistry`, and the offline DB so
 * the same query consistently maps to the same logic/state instance and the same persisted row
 * across runs.
 *
 * Two shapes are supported:
 *  - [Standard] for classic queries where the client knows `filter` + `querySort` upfront.
 *  - [Predefined] for server-side predefined filters where the actual `filter` and `querySort` are
 *    only learned from the response. Identity must therefore be the predefined name plus the
 *    interpolation values, since those are the only stable inputs available before the response.
 */
@InternalStreamChatApi
public sealed interface QueryChannelsIdentifier {

    /**
     * Identity for a classic query channels request: [filter] and [sort] are known on the client
     * and define the query.
     */
    public data class Standard(
        val filter: FilterObject,
        val sort: QuerySorter<Channel>,
    ) : QueryChannelsIdentifier

    /**
     * Identity for a server-side predefined filter: the actual filter and sort are resolved by the
     * backend; identity is the predefined [name] plus the value maps used to interpolate it.
     */
    public data class Predefined(
        val name: String,
        val filterValues: Map<String, Any>?,
        val sortValues: Map<String, Any>?,
    ) : QueryChannelsIdentifier
}

/**
 * Derives the [QueryChannelsIdentifier] from a [QueryChannelsRequest]. A non-null
 * [QueryChannelsRequest.predefinedFilter] marks the request as a predefined-filter query and
 * yields [QueryChannelsIdentifier.Predefined]; otherwise yields [QueryChannelsIdentifier.Standard]
 * from the explicit `filter`/`querySort`.
 */
internal val QueryChannelsRequest.identifier: QueryChannelsIdentifier
    get() = when (val name = predefinedFilter) {
        null -> QueryChannelsIdentifier.Standard(filter, querySort)
        else -> QueryChannelsIdentifier.Predefined(
            name = name,
            filterValues = filterValues.normalizedIdentifierValues(),
            sortValues = sortValues.normalizedIdentifierValues(),
        )
    }

/**
 * Derives the [QueryChannelsIdentifier] from a [QueryChannelsSpec]. A non-null
 * [QueryChannelsSpec.predefinedFilterName] marks the spec as a predefined-filter query and yields
 * [QueryChannelsIdentifier.Predefined]; otherwise yields [QueryChannelsIdentifier.Standard] from
 * the resolved `filter`/`querySort`.
 */
internal val QueryChannelsSpec.identifier: QueryChannelsIdentifier
    get() = when (val name = predefinedFilterName) {
        null -> QueryChannelsIdentifier.Standard(filter, querySort)
        else -> QueryChannelsIdentifier.Predefined(
            name = name,
            filterValues = predefinedFilterValues.normalizedIdentifierValues(),
            sortValues = predefinedSortValues.normalizedIdentifierValues(),
        )
    }

private fun Map<String, Any>?.normalizedIdentifierValues(): Map<String, Any>? =
    if (isNullOrEmpty()) null else this
