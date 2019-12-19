package io.getstream.chat.android.core.poc.app.repositories

import io.getstream.chat.android.core.poc.app.ChannelsCache
import io.getstream.chat.android.core.poc.app.common.ApiMapper
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.library.Client
import io.reactivex.Completable
import io.reactivex.Observable

class ChannelsRepositoryRx(
    private val client: Client,
    private val cache: ChannelsCache
) {

    fun getChannels(): Observable<List<Channel>> {
        return Observable.merge(getCached(), updateFromNetwork())
    }

    private fun updateFromNetwork(): Observable<List<Channel>> {
        return Completable.fromAction {
            val result = client.queryChannels().execute()

            if (result.isSuccess()) {
                cache.storeSync(ApiMapper.mapChannels(result.data()))
            } else {
                throw RuntimeException("Channels loading error")
            }

        }.toObservable()

    }

    private fun getCached(): Observable<List<Channel>> {
        return cache.getAllRx().distinct().flatMap {
            if (it.isEmpty()) {
                Observable.empty()
            } else {
                Observable.just(it)
            }
        }
    }
}