package com.getstream.sdk.chat.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

internal class EndlessScrollListener(
    private val loadMoreListener: OnLoadMoreListener
) : RecyclerView.OnScrollListener() {

    var paginationEnabled: Boolean = false
    var loadMoreThreshold: Int = 0
        set(value) {
            require(value >= 0) { "Load more threshold must not be negative" }
            field = value
        }

    private var scrollStateReset = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy >= 0 || !paginationEnabled) {
            return
        }
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            if (scrollStateReset && it.findFirstVisibleItemPosition() <= loadMoreThreshold) {
                scrollStateReset = false
                recyclerView.post {
                    if (paginationEnabled) {
                        loadMoreListener()
                    }
                }
            }
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE ||
            newState == RecyclerView.SCROLL_STATE_DRAGGING
        ) {
            scrollStateReset = true
        }
    }
}

internal typealias OnLoadMoreListener = () -> Unit
