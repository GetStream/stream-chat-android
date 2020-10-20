package io.getstream.chat.android.client.sample.cache

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.getstream.chat.android.client.sample.common.Channel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ChannelsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChannelRx(channel: Channel): Completable

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(channel: Channel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(channel: Channel): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChannelRx(channels: List<Channel>): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(channels: List<Channel>)

    @Query("select * from channels")
    fun getAllSync(): List<Channel>

    @Query("select * from channels")
    fun getAllLive(): LiveData<List<Channel>>

    @Query("select * from channels order by updated_at")
    fun getAllRx(): Observable<List<Channel>>

    @Query("select * from channels order by updated_at limit :limit offset :offset")
    fun getPageRx(offset: Int, limit: Int): Observable<List<Channel>>

    @Query("select * from channels where id = :id limit 1")
    fun getById(id: String): Channel?

    @Query("select * from channels where remote_id = :id limit 1")
    fun getByRemoteId(id: String): Channel?

    @Query("delete from channels")
    fun deleteAll(): Completable

    @Query("delete from channels where remote_id = :remoteId")
    fun delete(remoteId: String): Single<Int>

    @Query("delete from channels where remote_id between :fromRemoteId and :toRemoteId")
    fun delete(fromRemoteId: String, toRemoteId: String): Single<Int>

    @Transaction
    fun upsert(channels: List<Channel>) {
        channels.forEach { upsert(it) }
    }

    @Transaction
    fun upsert(channel: Channel) {

        if (channel.remoteId.isEmpty()) {
            channel.synched = false
            insert(channel)
        } else {
            val ch = getByRemoteId(channel.remoteId)
            channel.synched = true
            if (ch == null) {
                insert(channel)
            } else {

                if (isNew(channel, ch)) {
                    channel.id = ch.id
                    val updated = update(channel)
                    Log.d("channels-dao", updated.toString())
                }
            }
        }
    }

    @Transaction
    fun isNew(
        new: Channel,
        cached: Channel
    ) = new.updatedAt != cached.updatedAt
}
