package io.getstream.chat.android.client.sample

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.sample.cache.ChannelsDao
import io.getstream.chat.android.client.sample.common.Channel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class ChannelsCache(val dao: ChannelsDao) {

    fun getPageRx(offset: Int, limit: Int): Observable<List<Channel>> {
        return dao.getPageRx(offset, limit)
    }

    fun getAllRx(): Observable<List<Channel>> {
        return dao.getAllRx()
    }

    fun getAllSync(): List<Channel> {
        return dao.getAllSync()
    }

    fun getAllLive(): LiveData<List<Channel>> {
        return dao.getAllLive()
    }

    fun getById(id: String): Channel? {
        return dao.getById(id)
    }

    fun getByRemoteId(id: String): Channel? {
        return dao.getByRemoteId(id)
    }

    @SuppressLint("CheckResult")
    fun storeAsync(channels: List<Channel>) {
        Completable.fromAction {
            storeSync(channels)
        }.subscribeOn(Schedulers.io()).subscribe {
            Log.d(javaClass.simpleName, "channels stored")
        }
    }

    fun storeSync(channels: List<Channel>) {
        dao.upsert(channels)
    }
}
