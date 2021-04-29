package io.getstream.chat.android.offline.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback

internal class DiffUtilOperationCounter<T>(
    var diffCallback: (List<T>, List<T>) -> DiffUtil.DiffResult,
) : ListUpdateCallback {
    var counts = UpdateOperationCounts()
    var new: List<T>? = null
    var old: List<T>? = null

    fun onEvent(newValues: List<T>) {
        counts.events++

        new = newValues
        if (old != null && new != null) {
            val result = diffCallback(old!!, new!!)
            result.dispatchUpdatesTo(this)
        }
        old = new
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        counts.changed++
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        counts.moved++
    }

    override fun onInserted(position: Int, count: Int) {
        counts.inserted++
    }

    override fun onRemoved(position: Int, count: Int) {
        counts.removed++
    }
}

internal data class UpdateOperationCounts(
    var events: Int = 0,
    var changed: Int = 0,
    var moved: Int = 0,
    var inserted: Int = 0,
    var removed: Int = 0,
)
