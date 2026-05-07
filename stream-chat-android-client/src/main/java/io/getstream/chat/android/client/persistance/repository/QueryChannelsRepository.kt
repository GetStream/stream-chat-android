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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Repository for queries of channels.
 */
public interface QueryChannelsRepository {

    /**
     * Inserts a query channels.
     *
     * @param queryChannelsSpec [QueryChannelsSpec]
     */
    public suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec)

    /**
     * Selects by a filter and query sort. Kept for backwards compatibility with custom
     * implementations written before predefined filters existed.
     *
     * @param filter [FilterObject]
     * @param querySort [QuerySorter]
     */
    @Deprecated(
        message = "Use selectBy(QueryChannelsIdentifier) instead. " +
            "This overload cannot represent server-side predefined-filter queries.",
        replaceWith = ReplaceWith(
            "selectBy(QueryChannelsIdentifier.Standard(filter, querySort))",
            "io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier",
        ),
    )
    public suspend fun selectBy(filter: FilterObject, querySort: QuerySorter<Channel>): QueryChannelsSpec? = null

    /**
     * Selects a query spec by its identifier. Default implementation delegates to the legacy
     * [selectBy(filter, querySort)] for [QueryChannelsIdentifier.Standard] (so existing custom
     * implementations of the legacy overload keep working) and returns `null` for
     * [QueryChannelsIdentifier.Predefined] (no offline data — falls back to network).
     *
     * @param identifier The query spec identifier.
     */
    public suspend fun selectBy(identifier: QueryChannelsIdentifier): QueryChannelsSpec? = when (identifier) {
        is QueryChannelsIdentifier.Standard ->
            @Suppress("DEPRECATION")
            selectBy(identifier.filter, identifier.sort)
        is QueryChannelsIdentifier.Predefined -> null
    }

    /**
     * Clear QueryChannels of this repository.
     */
    public suspend fun clear()
}
