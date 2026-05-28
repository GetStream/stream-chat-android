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

package io.getstream.chat.android.offline.repository.domain.queryChannels.internal

import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.internal.state.plugin.identifier
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySorter

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

    override suspend fun selectBy(filter: FilterObject, querySort: QuerySorter<Channel>): QueryChannelsSpec? {
        return queryChannelsDao.select(generateId(QueryChannelsIdentifier.Standard(filter, querySort)))?.let(::toModel)
    }

    override suspend fun selectBy(
        predefinedFilterName: String,
        filterValues: Map<String, Any>?,
        sortValues: Map<String, Any>?,
    ): QueryChannelsSpec? {
        val identifier = QueryChannelsIdentifier.Predefined(predefinedFilterName, filterValues, sortValues)
        return queryChannelsDao.select(generateId(identifier))?.let(::toModel)
    }

    override suspend fun selectBy(groupKey: String): QueryChannelsSpec? {
        val identifier = QueryChannelsIdentifier.Grouped(groupKey)
        return queryChannelsDao.select(generateId(identifier))?.let(::toModel)
    }

    override suspend fun clear() {
        queryChannelsDao.deleteAll()
    }

    private companion object {
        // Standard: hash of (filter, sort). Predefined: name + value-map hashes, since the
        // resolved filter/sort are unknown until the server replies and we need stable identity
        // across runs. Grouped: stable groupKey returned by the server.
        private fun generateId(identifier: QueryChannelsIdentifier): String = when (identifier) {
            is QueryChannelsIdentifier.Standard ->
                "${identifier.filter.hashCode()}-${identifier.sort.toDto().hashCode()}"
            is QueryChannelsIdentifier.Predefined ->
                "pd:${identifier.name}:${identifier.filterValues.hashCode()}:${identifier.sortValues.hashCode()}"
            is QueryChannelsIdentifier.Grouped ->
                "grp:${identifier.groupKey}"
        }

        private fun toEntity(spec: QueryChannelsSpec): QueryChannelsEntity =
            QueryChannelsEntity(
                id = generateId(spec.identifier),
                filter = spec.filter,
                querySort = spec.querySort,
                cids = spec.cids.toList(),
                groupKey = spec.groupKey,
                predefinedFilterName = spec.predefinedFilterName,
                predefinedFilterValues = spec.predefinedFilterValues,
                predefinedSortValues = spec.predefinedSortValues,
            )

        private fun toModel(entity: QueryChannelsEntity): QueryChannelsSpec =
            QueryChannelsSpec(
                filter = entity.filter,
                querySort = entity.querySort,
                groupKey = entity.groupKey,
                predefinedFilterName = entity.predefinedFilterName,
                predefinedFilterValues = entity.predefinedFilterValues,
                predefinedSortValues = entity.predefinedSortValues,
            ).also { it.cids = entity.cids.toSet() }
    }
}
