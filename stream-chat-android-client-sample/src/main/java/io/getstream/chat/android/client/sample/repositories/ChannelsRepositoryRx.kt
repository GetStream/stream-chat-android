package io.getstream.chat.android.client.sample.repositories

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.sample.ChannelsCache
import io.getstream.chat.android.client.sample.common.Channel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.merge

class ChannelsRepositoryRx(
    private val client: ChatClient,
    private val cache: ChannelsCache
) {

    fun getChannels(offset: Int, limit: Int): Observable<List<Channel>> {

        var updatingCompleted = false

        return merge(
            getCached(offset, limit)
                .flatMap {
                    if (it.isEmpty() && !updatingCompleted) {
                        Observable.empty()
                    } else {
                        Observable.just(it)
                    }
                },
            updateCache(offset, limit)
                .doOnComplete {
                    updatingCompleted = true
                }
                .toObservable()
        )
    }

    private fun updateCache(offset: Int, limit: Int): Completable {

        return null!!
//        return fromAction {
//
//            val result = client.queryChannels(ChannelsQuery().apply {
//                this.offset = offset
//                this.limit = limit
//            }).execute()
//
//            if (result.isSuccess()) {
//                cache.storeSync(ApiMapper.mapChannels(result.data()))
//            } else {
//                throw RuntimeException("Channels loading error", result.error())
//            }
//        }
    }

    private fun getCached(offset: Int, limit: Int): Observable<List<Channel>> {
        return cache.getPageRx(offset, limit).flatMap {
            Observable.just(it)
        }
    }
}
