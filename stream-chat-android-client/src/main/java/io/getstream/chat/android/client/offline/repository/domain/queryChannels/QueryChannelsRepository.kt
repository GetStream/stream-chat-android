package io.getstream.chat.android.client.offline.repository.domain.queryChannels

import io.getstream.chat.android.client.offline.model.QueryChannelsSpec

internal interface QueryChannelsRepository {
    suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec)
    suspend fun selectById(id: String): QueryChannelsSpec?
}

internal class QueryChannelsRepositoryImpl(private val queryChannelsDao: QueryChannelsDao) : QueryChannelsRepository {
    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        queryChannelsDao.insert(toEntity(queryChannelsSpec))
    }

    override suspend fun selectById(id: String): QueryChannelsSpec? {
        return queryChannelsDao.select(id)?.let(Companion::toModel)
    }

    companion object {
        private fun toEntity(queryChannelsSpec: QueryChannelsSpec): QueryChannelsEntity =
            QueryChannelsEntity(queryChannelsSpec.id, queryChannelsSpec.filter, queryChannelsSpec.cids)

        private fun toModel(queryChannelsEntity: QueryChannelsEntity): QueryChannelsSpec =
            QueryChannelsSpec(
                queryChannelsEntity.filter,
                queryChannelsEntity.cids
            )
    }
}
