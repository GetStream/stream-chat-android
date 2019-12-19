package io.getstream.chat.android.core.poc.app.cache

import androidx.lifecycle.LiveData
import androidx.room.*
import io.getstream.chat.android.core.poc.app.common.Channel
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface ChannelsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannelRx(channel: Channel): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(channel: Channel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(channel: Channel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannelRx(channels: List<Channel>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(channels: List<Channel>)

    @Query("select * from channels")
    fun getAllSync(): List<Channel>

    @Query("select * from channels")
    fun getAllLive(): LiveData<List<Channel>>

    @Query("select * from channels")
    fun getAllRx(): Observable<List<Channel>>

    @Query("select * from channels where id = :id limit 1")
    fun getById(id: String): Channel?

    @Query("select * from channels where remote_id = :id limit 1")
    fun getByRemoteId(id: String): Channel?

    @Query("delete from channels")
    fun deleteAll(): Completable
}