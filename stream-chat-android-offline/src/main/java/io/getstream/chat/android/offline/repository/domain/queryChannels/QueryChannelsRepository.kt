package io.getstream.chat.android.offline.repository.domain.queryChannels

import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec

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
            QueryChannelsEntity(queryChannelsSpec.id, queryChannelsSpec.filter, queryChannelsSpec.querySort, queryChannelsSpec.cids.toList())

        private fun toModel(queryChannelsEntity: QueryChannelsEntity): QueryChannelsSpec =
            QueryChannelsSpec(queryChannelsEntity.filter, queryChannelsEntity.querySort).apply { queryChannelsEntity.cids.toSet() }
    }
}
