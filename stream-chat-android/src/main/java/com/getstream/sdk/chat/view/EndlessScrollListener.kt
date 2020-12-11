package com.getstream.sdk.chat.view

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

public class EndlessScrollListener(
    private val loadMoreThreshold: Int,
    private val loadMoreListener: () -> Unit,
) : RecyclerView.OnScrollListener() {

    init {
        require(loadMoreThreshold >= 0) { "Load more threshold must not be negative" }
    }

    private var paginationEnabled: Boolean = false
    private var scrollStateReset = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!paginationEnabled) {
            return
        }

        val layoutManager = recyclerView.layoutManager
        if (layoutManager !is LinearLayoutManager) {
            throw IllegalStateException("EndlessScrollListener supports only LinearLayoutManager")
        }

        if (layoutManager.stackFromEnd) {
            checkScrollUp(dy, layoutManager, recyclerView)
        } else {
            checkScrollDown(dy, layoutManager, recyclerView)
        }
    }

    private fun checkScrollUp(dy: Int, layoutManager: LinearLayoutManager, recyclerView: RecyclerView) {
        if (dy >= 0) {
            // Scrolling downwards
            return
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

    private fun checkScrollDown(dy: Int, layoutManager: LinearLayoutManager, recyclerView: RecyclerView) {
        if (dy <= 0) {
            // Scrolling upwards
            return
        }
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        val remainingItems = layoutManager.itemCount - lastVisiblePosition
        if (scrollStateReset && remainingItems <= loadMoreThreshold) {
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

    public fun enablePagination() {
        paginationEnabled = true
    }

    public fun disablePagination() {
        paginationEnabled = false
    }
}
