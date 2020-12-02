package com.getstream.sdk.chat.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

public class EndlessScrollListener(
    private val loadMoreListener: () -> Unit
) : RecyclerView.OnScrollListener() {

    public var paginationEnabled: Boolean = false
    public var loadMoreThreshold: Int = 0
        set(value) {
            require(value >= 0) { "Load more threshold must not be negative" }
            field = value
        }

    private var scrollStateReset = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy >= 0 || !paginationEnabled) {
            return
        }

        val layoutManager = recyclerView.layoutManager
        if (layoutManager !is LinearLayoutManager) {
            throw IllegalStateException("EndlessScrollListener supports only LinearLayoutManager")
        }
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        if (scrollStateReset && firstVisiblePosition <= loadMoreThreshold) {
            scrollStateReset = false
            recyclerView.post {
                if (paginationEnabled) {
                    loadMoreListener()
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
