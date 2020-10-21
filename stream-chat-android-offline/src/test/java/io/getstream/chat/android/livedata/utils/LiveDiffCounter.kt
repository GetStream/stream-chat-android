package io.getstream.chat.android.livedata.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback

internal class LiveDiffCounter<T>(var diffCallback: (List<T>, List<T>) -> DiffUtil.DiffResult) : ListUpdateCallback {
    var counts = mutableMapOf("events" to 0, "changed" to 0, "moved" to 0, "inserted" to 0, "removed" to 0)
    var new: List<T>? = null
    var old: List<T>? = null

    fun onEvent(newValues: List<T>) {
        counts["events"] = counts["events"]!! + 1

        new = newValues
        if (old != null && new != null) {
            val result = diffCallback(old!!, new!!)
            result.dispatchUpdatesTo(this)
        }
        old = new
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        counts["changed"] = counts["changed"]!! + 1
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        counts["moved"] = counts["moved"]!! + 1
    }

    override fun onInserted(position: Int, count: Int) {
        counts["inserted"] = counts["inserted"]!! + 1
    }

    override fun onRemoved(position: Int, count: Int) {
        counts["removed"] = counts["removed"]!! + 1
    }
}
