package io.getstream.chat.android.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity

@Dao
internal interface QueryChannelsDao {
    /*
    - query channels -> write the query, write many channels
    - notification.new event -> update a single channel
    - offline read flow -> query id based lookup, read a list of channels
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(queryChannelsEntity: QueryChannelsEntity)

    @Query(
        "SELECT * FROM stream_channel_query " +
            "WHERE stream_channel_query.id=:id"
    )
    suspend fun select(id: String): QueryChannelsEntity?

    @Query(
        "SELECT * FROM stream_channel_query " +
            "WHERE stream_channel_query.id IN (:ids)"
    )
    suspend fun select(ids: List<String>): List<QueryChannelsEntity>
}
