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
        private fun generateId(identifier: QueryChannelsIdentifier): String = when (identifier) {
            is QueryChannelsIdentifier.Standard ->
                "${identifier.filter.hashCode()}-${identifier.sort.toDto().hashCode()}"
            is QueryChannelsIdentifier.Grouped ->
                "grp:${identifier.group}"
        }

        private fun toEntity(spec: QueryChannelsSpec): QueryChannelsEntity = QueryChannelsEntity(
            id = generateId(spec.identifier),
            filter = spec.filter,
            querySort = spec.querySort,
            cids = spec.cids.toList(),
            groupKey = spec.groupKey,
        )

        private fun toModel(entity: QueryChannelsEntity): QueryChannelsSpec = QueryChannelsSpec(
            filter = entity.filter,
            querySort = entity.querySort,
            cids = entity.cids.toSet(),
            groupKey = entity.groupKey,
        )
    }
}
