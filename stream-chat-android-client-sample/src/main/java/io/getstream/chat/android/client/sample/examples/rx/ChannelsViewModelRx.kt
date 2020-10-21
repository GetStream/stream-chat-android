package io.getstream.chat.android.client.sample.examples.rx

import io.getstream.chat.android.client.sample.ViewState
import io.getstream.chat.android.client.sample.ViewState.Success
import io.getstream.chat.android.client.sample.common.Channel
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositoryRx
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ChannelsViewModelRx(val repository: ChannelsRepositoryRx) {

    fun channels(offset: Int, limit: Int): Observable<ViewState<List<Channel>>> {
        return loadChannels(offset, limit)
    }

    private fun loadChannels(
        offset: Int,
        limit: Int
    ): Observable<ViewState<List<Channel>>> {
        return repository.getChannels(offset, limit)
            .map<ViewState<List<Channel>>> { Success(it) }
            .startWith(ViewState.Loading())
            .onErrorReturn { ViewState.Error(it) }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}
