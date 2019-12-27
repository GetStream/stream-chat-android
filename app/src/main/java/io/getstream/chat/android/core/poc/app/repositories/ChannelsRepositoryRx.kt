package io.getstream.chat.android.core.poc.app.repositories

import io.getstream.chat.android.core.poc.app.ChannelsCache
import io.getstream.chat.android.core.poc.app.common.ApiMapper
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.library.StreamChatClient
import io.getstream.chat.android.core.poc.library.requests.ChannelsQuery
import io.reactivex.Completable
import io.reactivex.Completable.fromAction
import io.reactivex.Observable
import io.reactivex.Observable.merge

class ChannelsRepositoryRx(
    private val client: StreamChatClient,
    private val cache: ChannelsCache
) {

    fun getChannels(offset: Int, limit: Int): Observable<List<Channel>> {

        var updatingCompleted = false

        return merge(getCached(offset, limit)
            .flatMap {
                if (it.isEmpty() && !updatingCompleted) {
                    Observable.empty()
                } else {
                    Observable.just(it)
                }
            }, updateCache(offset, limit)
            .doOnComplete {
                updatingCompleted = true
            }
            .toObservable())
    }

    private fun updateCache(offset: Int, limit: Int): Completable {
        return fromAction {

            val result = client.queryChannels(ChannelsQuery().apply {
                this.offset = offset
                this.limit = limit
            }).execute()

            if (result.isSuccess()) {
                cache.storeSync(ApiMapper.mapChannels(result.data()))
            } else {
                throw RuntimeException("Channels loading error", result.error())
            }
        }
    }

    private fun getCached(offset: Int, limit: Int): Observable<List<Channel>> {
        return cache.getPageRx(offset, limit).distinct().flatMap {
            Observable.just(it)
        }
    }

}