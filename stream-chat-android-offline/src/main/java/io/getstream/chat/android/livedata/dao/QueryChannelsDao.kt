package io.getstream.chat.android.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.getstream.chat.android.livedata.entity.ChannelSortInnerEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsWithSorts

@Dao
internal interface QueryChannelsDao {
    /*
    - query channels -> write the query, write many channels
    - notification.new event -> update a single channel
    - offline read flow -> query id based lookup, read a list of channels
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(queryChannelsEntity: QueryChannelsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sortInnerEntity: ChannelSortInnerEntity)

    @Transaction
    suspend fun insert(queryWithSorts: QueryChannelsWithSorts) {
        insert(queryWithSorts.query)
        queryWithSorts.sortInnerEntities.forEach { sortEntity -> insert(sortEntity) }
    }

    @Transaction
    @Query(
        "SELECT * FROM stream_channel_query " +
            "WHERE stream_channel_query.id=:id"
    )
    suspend fun select(id: String): QueryChannelsWithSorts?

    @Transaction
    @Query(
        "SELECT * FROM stream_channel_query " +
            "WHERE stream_channel_query.id IN (:ids)"
    )
    suspend fun select(ids: List<String>): List<QueryChannelsWithSorts>
}
