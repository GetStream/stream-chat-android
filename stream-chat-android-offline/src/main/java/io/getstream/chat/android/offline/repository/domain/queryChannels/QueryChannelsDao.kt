package io.getstream.chat.android.offline.repository.domain.queryChannels

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
internal abstract class QueryChannelsDao {
    /*
    - query channels -> write the query, write many channels
    - notification.new event -> update a single channel
    - offline read flow -> query id based lookup, read a list of channels
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(queryChannelsEntity: QueryChannelsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(sortInnerEntities: List<ChannelSortInnerEntity>)

    @Transaction
    open suspend fun insert(queryWithSorts: QueryChannelsWithSorts) {
        deleteSortEntitiesFor(queryWithSorts.query.id)
        insert(queryWithSorts.query)
        insert(queryWithSorts.sortInnerEntities)
    }

    @Transaction
    @Query("DELETE FROM channel_sort_inner_entity WHERE queryId = :queryId")
    abstract suspend fun deleteSortEntitiesFor(queryId: String)

    @Transaction
    @Query("SELECT * FROM stream_channel_query WHERE stream_channel_query.id=:id")
    abstract suspend fun select(id: String): QueryChannelsWithSorts?

    @Transaction
    @Query("SELECT * FROM stream_channel_query WHERE stream_channel_query.id IN (:ids)")
    abstract suspend fun select(ids: List<String>): List<QueryChannelsWithSorts>
}
