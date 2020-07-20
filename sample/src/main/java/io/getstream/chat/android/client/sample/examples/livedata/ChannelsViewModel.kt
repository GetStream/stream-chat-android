package io.getstream.chat.android.client.sample.examples.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.sample.ViewState
import io.getstream.chat.android.client.sample.common.Channel
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositoryLive

class ChannelsViewModel(private val repository: ChannelsRepositoryLive) {

    fun channels(): LiveData<ViewState<List<Channel>>> {

        val liveData = MediatorLiveData<ViewState<List<Channel>>>()

        liveData.addSource(MutableLiveData<ViewState<List<Channel>>>(ViewState.Loading())) {
            liveData.value = it
        }

        liveData.addSource(
            Transformations.map(repository.getChannels()) { channels ->
                ViewState.Success(channels)
            }
        ) {
            if (it.data.isNotEmpty()) liveData.value = it
        }

        return liveData
    }
}
