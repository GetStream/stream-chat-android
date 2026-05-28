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
     * Selects a query spec persisted for a classic query identified by [filter] and [querySort].
     *
     * @param filter [FilterObject]
     * @param querySort [QuerySorter]
     */
    public suspend fun selectBy(filter: FilterObject, querySort: QuerySorter<Channel>): QueryChannelsSpec? = null

    /**
     * Selects a query spec persisted for a server-side predefined-filter query identified by
     * [predefinedFilterName] and its interpolation values.
     *
     * @param predefinedFilterName Name of the predefined filter registered on the backend.
     * @param filterValues Values interpolated into the predefined filter template. `null` and an
     *   empty map are treated as equivalent — pass normalized maps for stable identity.
     * @param sortValues Values interpolated into the predefined sort template. Same normalization
     *   contract as [filterValues].
     */
    public suspend fun selectBy(
        predefinedFilterName: String,
        filterValues: Map<String, Any>?,
        sortValues: Map<String, Any>?,
    ): QueryChannelsSpec? = null

    /**
     * Clear QueryChannels of this repository.
     */
    public suspend fun clear()
}
