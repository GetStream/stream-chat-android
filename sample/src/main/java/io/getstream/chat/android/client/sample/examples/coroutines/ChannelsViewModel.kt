package io.getstream.chat.android.client.sample.examples.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.getstream.chat.android.client.sample.ViewState
import io.getstream.chat.android.client.sample.common.Channel
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositorySync
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelsViewModel(repository: ChannelsRepositorySync) {

    val channels = MutableLiveData<ViewState<List<Channel>>>()

    val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    val bgDispatcher: CoroutineDispatcher = Dispatchers.IO

    val uiScope = CoroutineScope(uiDispatcher)
    val bgScope = CoroutineScope(bgDispatcher)

    init {

        channels.postValue(ViewState.Loading())

        uiScope.launch {
            val result = withContext(bgDispatcher) { repository.getChannels() }
            channels.postValue(ViewState.Success(result))
        }
    }

    fun channels(): LiveData<ViewState<List<Channel>>> {
        return channels
    }
}
