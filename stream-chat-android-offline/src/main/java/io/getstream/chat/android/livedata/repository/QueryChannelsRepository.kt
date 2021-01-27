package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QuerySort.Companion.ascByName
import io.getstream.chat.android.client.api.models.QuerySort.Companion.descByName
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.dao.QueryChannelsDao
import io.getstream.chat.android.livedata.entity.ChannelSortInnerEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsWithSorts
import java.util.Objects

internal class QueryChannelsRepository(private val queryChannelsDao: QueryChannelsDao) {
    suspend fun insert(queryChannelsSpec: QueryChannelsSpec) {
        queryChannelsDao.insert(toEntity(queryChannelsSpec))
    }

    suspend fun selectByFilterAndQuerySort(queryChannelsSpec: QueryChannelsSpec): QueryChannelsSpec? {
        return selectById(getId(queryChannelsSpec))
    }

    suspend fun selectById(id: String): QueryChannelsSpec? {
        return queryChannelsDao.select(id)?.let(::toModel)
    }

    suspend fun selectById(ids: List<String>): List<QueryChannelsSpec> {
        return queryChannelsDao.select(ids).map(::toModel)
    }

    suspend fun selectByFilterAndQuerySort(queries: List<QueryChannelsSpec>): List<QueryChannelsSpec> {
        return selectById(queries.map(::getId))
    }

    companion object {
        // TODO consider how to make it private
        internal fun getId(queryChannelsSpec: QueryChannelsSpec): String {
            return (Objects.hash(queryChannelsSpec.filter.toMap()) + Objects.hash(queryChannelsSpec.sort.toDto())).toString()
        }

        private fun toEntity(queryChannelsSpec: QueryChannelsSpec): QueryChannelsWithSorts {
            val queryEntity =
                QueryChannelsEntity(getId(queryChannelsSpec), queryChannelsSpec.filter, queryChannelsSpec.cids)
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
                queryWithSort.sortInnerEntities.let(::restoreQuerySort),
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
