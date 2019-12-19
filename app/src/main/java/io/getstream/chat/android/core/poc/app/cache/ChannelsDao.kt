package io.getstream.chat.android.core.poc.app.cache

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import io.getstream.chat.android.core.poc.app.common.Channel
import io.reactivex.Completable
import io.reactivex.Observable

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

    @Query("select * from channels where id = :id limit 1")
    fun getById(id: String): Channel?

    @Query("select * from channels where remote_id = :id limit 1")
    fun getByRemoteId(id: String): Channel?

    @Query("delete from channels")
    fun deleteAll(): Completable

    @Transaction
    fun upsert(channels: List<Channel>) {
        channels.forEach { storeSync(it) }
    }

    @Transaction
    fun storeSync(channel: Channel) {

        if (channel.remoteId.isEmpty()) {
            channel.synched = false
            insert(channel)
        } else {
            val ch = getByRemoteId(channel.remoteId)
            channel.synched = true
            if (ch == null) {
                insert(channel)
            } else {

                if (channel.updatedAt != ch.updatedAt) {
                    channel.id = ch.id
                    val updated = update(channel)
                    Log.d("channels-dao", updated.toString())
                }
            }
        }
    }

}