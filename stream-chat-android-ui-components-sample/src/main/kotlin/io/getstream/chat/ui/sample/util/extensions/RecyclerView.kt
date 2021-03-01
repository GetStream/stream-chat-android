package io.getstream.chat.ui.sample.util.extensions

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Automatically scrolls [RecyclerView] to the top when the first item is completely visible and
 * a new range of items is inserted above.
 */
internal fun RecyclerView.autoScrollToTop() {
    val layoutManager = layoutManager as? LinearLayoutManager
        ?: throw IllegalStateException("Auto scroll only works with LinearLayoutManager")
    val adapter = adapter
        ?: throw IllegalStateException("Adapter must be set in order for auto scroll to work")

    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            if (positionStart == 0 && positionStart == layoutManager.findFirstCompletelyVisibleItemPosition()) {
                layoutManager.scrollToPosition(0)
            }
        }
    })
}
