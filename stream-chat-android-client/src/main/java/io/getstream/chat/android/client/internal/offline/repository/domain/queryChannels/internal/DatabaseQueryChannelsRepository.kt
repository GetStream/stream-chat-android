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

package io.getstream.chat.android.client.internal.offline.repository.domain.queryChannels.internal

import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.internal.state.plugin.identifier
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.query.QueryChannelsSpec

/**
 * Repository for queries of channels. This implementation uses the database.
 */
internal class DatabaseQueryChannelsRepository(
    private val queryChannelsDao: QueryChannelsDao,
) : QueryChannelsRepository {

    /**
     * Inserts a query channels.
     *
     * @param queryChannelsSpec [QueryChannelsSpec]
     */
    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        queryChannelsDao.insert(toEntity(queryChannelsSpec))
    }

    override suspend fun selectBy(identifier: QueryChannelsIdentifier): QueryChannelsSpec? {
        return queryChannelsDao.select(generateId(identifier))?.let(Companion::toModel)
    }

    override suspend fun clear() {
        queryChannelsDao.deleteAll()
    }

    private companion object {
        // Standard: hash of (filter, sort). Predefined: name + value-map hashes, since the
        // resolved filter/sort are unknown until the server replies and we need stable identity
        // across runs.
        private fun generateId(identifier: QueryChannelsIdentifier): String = when (identifier) {
            is QueryChannelsIdentifier.Standard ->
                "${identifier.filter.hashCode()}-${identifier.sort.toDto().hashCode()}"
            is QueryChannelsIdentifier.Predefined ->
                "pd:${identifier.name}:${identifier.filterValues.hashCode()}:${identifier.sortValues.hashCode()}"
        }

        private fun toEntity(queryChannelsSpec: QueryChannelsSpec): QueryChannelsEntity =
            QueryChannelsEntity(
                id = generateId(queryChannelsSpec.identifier),
                filter = queryChannelsSpec.filter,
                querySort = queryChannelsSpec.querySort,
                cids = queryChannelsSpec.cids.toList(),
                predefinedFilterName = queryChannelsSpec.predefinedFilterName,
                predefinedFilterValues = queryChannelsSpec.predefinedFilterValues,
                predefinedSortValues = queryChannelsSpec.predefinedSortValues,
            )

        private fun toModel(queryChannelsEntity: QueryChannelsEntity): QueryChannelsSpec =
            QueryChannelsSpec.create(
                filter = queryChannelsEntity.filter,
                querySort = queryChannelsEntity.querySort,
                cids = queryChannelsEntity.cids.toSet(),
                predefinedFilterName = queryChannelsEntity.predefinedFilterName,
                predefinedFilterValues = queryChannelsEntity.predefinedFilterValues,
                predefinedSortValues = queryChannelsEntity.predefinedSortValues,
            )
    }
}
