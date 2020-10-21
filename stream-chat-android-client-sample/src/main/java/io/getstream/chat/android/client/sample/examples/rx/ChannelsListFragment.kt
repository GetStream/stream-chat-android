package io.getstream.chat.android.client.sample.examples.rx

import android.os.Bundle
import android.view.View
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.ViewState
import io.getstream.chat.android.client.sample.common.BaseChannelsListFragment
import io.getstream.chat.android.client.sample.common.Channel
import io.getstream.chat.android.client.sample.utils.PaginationListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_channels.*

class ChannelsListFragment : BaseChannelsListFragment() {

    var subs = CompositeDisposable()
    val vm = ChannelsViewModelRx(App.channelsRepositoryRx)
    var isLastPage = false
    var isLoading = false

    var offset = 0
    var pageSize = 15 // TODO: define on layout pass

    private val alreadyThere = mutableSetOf<Int>()

    private val adapter = PageAdapter()

    override fun reload() {
        offset = 0
        isLoading = false
        isLastPage = false
        alreadyThere.clear()
        subs.dispose()
        subs = CompositeDisposable()
        adapter.clear()
        loadNextPage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        recyclerChannels.addOnScrollListener(
            object : PaginationListener(10) {

                override fun loadMoreItems() {
                    loadNextPage()
                }

                override fun isLastPage(): Boolean {
                    return isLastPage
                }

                override fun isLoading(): Boolean {
                    return isLoading
                }
            }
        )

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

        subs.add(
            vm.channels(offset, pageSize).subscribe {

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
                        // updateAdapter(it.data)

                        adapter.addPage(offset, it.data)

                        isLastPage = it.data.size < pageSize
                        isLoading = false
                        if (isLastPage) {
                            drawAllLoaded()
                        }
                    }
                }
            }
        )
    }

    override fun updateAdapter(channels: List<Channel>) {
    }

    override fun onDestroyView() {
        subs.dispose()
        super.onDestroyView()
    }
}
