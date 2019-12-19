package io.getstream.chat.android.core.poc.app.coroutines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.getstream.chat.android.core.poc.app.ViewState
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.app.repositories.ChannelsRepositorySync
import kotlinx.coroutines.*

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