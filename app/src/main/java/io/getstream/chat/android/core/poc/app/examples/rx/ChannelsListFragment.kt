package io.getstream.chat.android.core.poc.app.examples.rx

import android.os.Bundle
import android.view.View
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.app.ViewState
import io.getstream.chat.android.core.poc.app.common.BaseChannelsListFragment
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.app.common.ChannelsListAdapter
import io.getstream.chat.android.core.poc.app.utils.PaginationListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_channels.*


class ChannelsListFragment : BaseChannelsListFragment() {

    val subs = CompositeDisposable()
    val vm = ChannelsViewModelRx(App.channelsRepositoryRx)
    var isLastPage = false
    var isLoading = false

    var offset = 0
    var pageSize = 15 //TODO: define on layout pass

    private val alreadyThere = mutableSetOf<Int>()

    private val adapter = ChannelsListAdapter(emptyList())

    override fun reload() {
        offset = 0
        isLoading = false
        isLastPage = false
        alreadyThere.clear()
        subs.dispose()
        adapter.clear()
        loadNextPage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        recyclerChannels.addOnScrollListener(object : PaginationListener(10) {

            override fun loadMoreItems() {
                loadNextPage()
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        recyclerChannels.adapter = adapter

        loadNextPage()
    }

    private fun loadNextPage() {
        load(offset)
    }

    private fun load(offset: Int) {

        if (alreadyThere.contains(offset)) {
            return
        } else {
            this.offset += pageSize
            alreadyThere.add(offset)
        }

        isLoading = true

        subs.add(vm.channels(offset, pageSize).subscribe {

            val thisOffset = offset

            when (it) {
                is ViewState.Loading -> {
                    drawLoading()
                }
                is ViewState.Error -> {
                    drawError(it.error)
                    isLoading = false
                }
                is ViewState.Success -> {
                    drawSuccess(it.data)
                    updateAdapter(it.data)
                    isLastPage = it.data.size < pageSize
                    isLoading = false
                    if (isLastPage) {
                        drawAllLoaded()
                    }
                }
            }
        })
    }

    override fun updateAdapter(channels: List<Channel>) {
        adapter.setOrUpdate(channels, recyclerChannels)
    }

    override fun onDestroyView() {
        subs.dispose()
        super.onDestroyView()
    }
}