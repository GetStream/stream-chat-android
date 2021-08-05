package io.getstream.chat.android.ui.common.extensions.internal

import androidx.recyclerview.widget.RecyclerView

internal fun <VH : RecyclerView.ViewHolder, A : RecyclerView.Adapter<VH>> A.doForAllViewHolders(
    recyclerView: RecyclerView,
    action: (VH) -> Unit,
) {
    for (i in 0 until itemCount) {
        (recyclerView.findViewHolderForAdapterPosition(i) as? VH)?.let(action)
    }
}
