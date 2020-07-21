package io.getstream.chat.android.client.sample.examples.livedata

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.ViewState
import io.getstream.chat.android.client.sample.common.BaseChannelsListFragment
import io.getstream.chat.android.client.sample.common.Channel

class ChannelsListFragment : BaseChannelsListFragment() {

    override fun reload() {
        load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        load()
    }

    private fun load() {
        val vm = ChannelsViewModel(App.channelsRepositoryLive)

        vm.channels().observe(
            this,
            Observer<ViewState<List<Channel>>> { state ->
                when (state) {
                    is ViewState.Loading -> {
                        drawLoading()
                    }
                    is ViewState.Error -> {
                        drawError(state.error)
                    }
                    is ViewState.Success -> {
                        drawSuccess(state.data)
                    }
                }
            }
        )
    }
}
