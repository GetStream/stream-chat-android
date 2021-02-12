package io.getstream.chat.android.ui.channel.list.internal

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Exposes an API for pausing scroll functionality. Primary use-case is for pausing
 * a RecyclerView's ability to scroll when we are swiping items.
 */
internal class ScrollPauseLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    var verticalScrollEnabled: Boolean = orientation == VERTICAL
    var horizontalScrollEnabled: Boolean = orientation == HORIZONTAL

    override fun canScrollVertically(): Boolean {
        return verticalScrollEnabled && super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return horizontalScrollEnabled && super.canScrollHorizontally()
    }
}
