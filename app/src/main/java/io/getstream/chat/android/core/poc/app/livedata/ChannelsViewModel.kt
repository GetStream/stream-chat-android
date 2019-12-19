package io.getstream.chat.android.core.poc.app.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.core.poc.app.ViewState
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.app.repositories.ChannelsRepositoryLive


class ChannelsViewModel(private val repository: ChannelsRepositoryLive) {

    fun channels(): LiveData<ViewState<List<Channel>>> {

        val liveData = MediatorLiveData<ViewState<List<Channel>>>()

        liveData.addSource(MutableLiveData<ViewState<List<Channel>>>(ViewState.Loading())) {
            liveData.value = it
        }

        liveData.addSource(Transformations.map(repository.getChannels()) { channels ->
            ViewState.Success(channels)
        }) {
            if (it.data.isNotEmpty()) liveData.value = it
        }

        return liveData
    }

}