/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.model.querychannels.internal.QueryChannelsSpec

internal interface QueryChannelsRepository {
    suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec)
    suspend fun selectBy(filter: FilterObject, querySort: QuerySort<Channel>): QueryChannelsSpec?
}

internal class QueryChannelsRepositoryImpl(private val queryChannelsDao: QueryChannelsDao) : QueryChannelsRepository {
    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        queryChannelsDao.insert(toEntity(queryChannelsSpec))
    }

    override suspend fun selectBy(filter: FilterObject, querySort: QuerySort<Channel>): QueryChannelsSpec? {
        return queryChannelsDao.select(generateId(filter, querySort))?.let(Companion::toModel)
    }

    companion object {
        private fun generateId(filter: FilterObject, querySort: QuerySort<Channel>): String {
            return "${filter.hashCode()}-${querySort.toDto().hashCode()}"
        }

        private fun toEntity(queryChannelsSpec: QueryChannelsSpec): QueryChannelsEntity =
            QueryChannelsEntity(
                generateId(queryChannelsSpec.filter, queryChannelsSpec.querySort),
                queryChannelsSpec.filter,
                queryChannelsSpec.querySort,
                queryChannelsSpec.cids.toList()
            )

        private fun toModel(queryChannelsEntity: QueryChannelsEntity): QueryChannelsSpec =
            QueryChannelsSpec(
                queryChannelsEntity.filter,
                queryChannelsEntity.querySort
            ).apply { cids = queryChannelsEntity.cids.toSet() }
    }
}
