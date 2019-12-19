package io.getstream.chat.android.core.poc.app.coroutines

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.app.ViewState
import io.getstream.chat.android.core.poc.app.common.BaseChannelsListFragment
import io.getstream.chat.android.core.poc.app.common.Channel

class ChannelsListFragment : BaseChannelsListFragment() {
    override fun reload() {
        load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        load()
    }

    private fun load() {
        val vm = ChannelsViewModel(App.channelsRepositorySync)

        vm.channels().observe(this, Observer<ViewState<List<Channel>>> { state ->
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
        })
    }


}