package io.getstream.chat.android.core.poc.app.rx

import android.os.Bundle
import android.view.View
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.app.ViewState
import io.getstream.chat.android.core.poc.app.common.BaseChannelsListFragment
import io.reactivex.disposables.CompositeDisposable

class ChannelsListFragment : BaseChannelsListFragment() {

    val subs = CompositeDisposable()

    override fun reload() {
        load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        load()
    }

    private fun load() {
        val vm = ChannelsViewModelRx(App.channelsRepositoryRx)

        subs.add(vm.channels().subscribe {
            when (it) {
                is ViewState.Loading -> {
                    drawLoading()
                }
                is ViewState.Error -> {
                    drawError(it.error)
                }
                is ViewState.Success -> {
                    drawSuccess(it.data)
                }
            }
        })
    }

    override fun onDestroyView() {
        subs.dispose()
        super.onDestroyView()
    }
}