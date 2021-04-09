package io.getstream.chat.android.ui.common.internal

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Automatically scrolls [RecyclerView] to the top when the first item is completely visible and
 * a new range of items is inserted or moved to the position above.
 */
internal class SnapToTopDataObserver(
    private val recyclerView: RecyclerView,
) : RecyclerView.AdapterDataObserver() {

    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        ?: throw IllegalStateException("Auto scroll only works with LinearLayoutManager")

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        autoScrollToTopIfNecessary(minOf(fromPosition, toPosition))
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        autoScrollToTopIfNecessary(positionStart)
    }

    /**
     * Scrolls the list to the top if the user is is not dragging it
     * and the list is scrolled to the top prior to the update.
     */
    private fun autoScrollToTopIfNecessary(itemPosition: Int) {
        if (
            itemPosition != 0 ||
            recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE ||
            recyclerView.canScrollVertically(-1) ||
            layoutManager.findFirstVisibleItemPosition() == 0
        ) {
            return
        }

        layoutManager.scrollToPosition(0)
    }
}
