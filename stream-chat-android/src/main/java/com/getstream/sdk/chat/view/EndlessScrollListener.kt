package com.getstream.sdk.chat.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

internal class EndlessScrollListener(
    private var loadMoreThreshold: Int,
    private val loadMoreListener: OnLoadMoreListener
) : RecyclerView.OnScrollListener() {

    var enabled: Boolean = false

    private var scrollStateReset = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy >= 0 || !enabled) {
            return
        }
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            if (scrollStateReset && it.findFirstVisibleItemPosition() < loadMoreThreshold) {
                scrollStateReset = false
                recyclerView.post {
                    if (enabled) {
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
