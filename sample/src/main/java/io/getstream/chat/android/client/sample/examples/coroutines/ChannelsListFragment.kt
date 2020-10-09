package io.getstream.chat.android.client.sample.examples.coroutines

import android.os.Bundle
import android.view.View
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.ViewState
import io.getstream.chat.android.client.sample.common.BaseChannelsListFragment

class ChannelsListFragment : BaseChannelsListFragment() {
    override fun reload() {
        load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        load()
    }

    private fun load() {
        val vm = ChannelsViewModel(App.channelsRepositorySync)

        vm.channels().observe(viewLifecycleOwner) { state ->
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
    }
}
