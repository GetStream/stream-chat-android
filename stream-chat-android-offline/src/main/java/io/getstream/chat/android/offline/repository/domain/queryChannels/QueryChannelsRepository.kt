package io.getstream.chat.android.offline.repository.domain.queryChannels

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QuerySort.Companion.ascByName
import io.getstream.chat.android.client.api.models.QuerySort.Companion.descByName
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec

internal interface QueryChannelsRepository {
    suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec)
    suspend fun selectById(id: String): QueryChannelsSpec?
    suspend fun selectQueriesChannelsByIds(ids: List<String>): List<QueryChannelsSpec>
}

internal class QueryChannelsRepositoryImpl(private val queryChannelsDao: QueryChannelsDao) : QueryChannelsRepository {
    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        queryChannelsDao.insert(toEntity(queryChannelsSpec))
    }

    override suspend fun selectById(id: String): QueryChannelsSpec? {
        return queryChannelsDao.select(id)?.let(Companion::toModel)
    }

    override suspend fun selectQueriesChannelsByIds(ids: List<String>): List<QueryChannelsSpec> {
        return queryChannelsDao.select(ids).map(Companion::toModel)
    }

    companion object {
        private fun toEntity(queryChannelsSpec: QueryChannelsSpec): QueryChannelsWithSorts {
            val queryEntity =
                QueryChannelsEntity(queryChannelsSpec.id, queryChannelsSpec.filter, queryChannelsSpec.cids)
            val sortInnerEntities = queryChannelsSpec.sort.toList().map { (name, sortDirection) ->
                ChannelSortInnerEntity(
                    name,
                    sortDirection.value,
                    queryEntity.id
                )
            }
            return QueryChannelsWithSorts(queryEntity, sortInnerEntities)
        }

        private fun toModel(queryWithSort: QueryChannelsWithSorts): QueryChannelsSpec =
            QueryChannelsSpec(
                queryWithSort.query.filter,
                queryWithSort.sortInnerEntities.let(Companion::restoreQuerySort),
                queryWithSort.query.cids
            )

        private fun restoreQuerySort(querySortData: List<ChannelSortInnerEntity>): QuerySort<Channel> {
            return querySortData.fold(QuerySort()) { querySort, sortEntity ->
                when (sortEntity.direction) {
                    QuerySort.SortDirection.ASC.value -> querySort.ascByName(sortEntity.name)
                    QuerySort.SortDirection.DESC.value -> querySort.descByName(sortEntity.name)
                    else -> error("Direction value must be only asc or desc!")
                }
            }
        }
    }
}
