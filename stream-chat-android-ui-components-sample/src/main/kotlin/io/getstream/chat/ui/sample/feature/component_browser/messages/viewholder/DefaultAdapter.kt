package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

internal class DefaultAdapter<T, VH : RecyclerView.ViewHolder>(
    private val list: List<T>,
    private val viewHolderFactory: (ViewGroup) -> VH,
    private val binder: (VH, T) -> Unit
) : RecyclerView.Adapter<VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = viewHolderFactory(parent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        binder(holder, list[position])
    }

    override fun getItemCount(): Int = list.size
}
