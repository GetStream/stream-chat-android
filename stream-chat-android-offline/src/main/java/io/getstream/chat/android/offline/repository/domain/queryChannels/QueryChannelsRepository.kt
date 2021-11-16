package io.getstream.chat.android.offline.repository.domain.queryChannels

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec

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
