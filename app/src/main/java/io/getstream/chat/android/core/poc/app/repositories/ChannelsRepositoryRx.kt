package io.getstream.chat.android.core.poc.app.repositories

import io.getstream.chat.android.core.poc.app.ChannelsCache
import io.getstream.chat.android.core.poc.app.common.ApiMapper
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.library.Client
import io.reactivex.Completable
import io.reactivex.Completable.fromAction
import io.reactivex.Observable
import io.reactivex.Observable.merge

class ChannelsRepositoryRx(
    private val client: Client,
    private val cache: ChannelsCache
) {

    fun getChannels(): Observable<List<Channel>> {
        return merge(getCached(), updateCache())
    }

    private fun updateCache(): Observable<List<Channel>> {
        return fromAction {
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
            Observable.just(it)
        }
    }
}