package io.getstream.chat.android.ui.common.internal

import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView

public abstract class SimpleListAdapter<T : Any, VH : SimpleListAdapter.ViewHolder<T>> : RecyclerView.Adapter<VH>() {
    public var itemList: MutableList<T> = mutableListOf()

    final override fun getItemCount(): Int = itemList.size

    final override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(itemList[position])
    }

    public fun setItems(items: List<T>) {
        this.itemList.clear()
        this.itemList.addAll(items)
        notifyDataSetChanged()
    }

    public fun removeItem(item: T) {
        val index = itemList.indexOf(item)
        if (index != -1) {
            itemList.remove(item)
            notifyItemRemoved(index)
        }
    }

    public fun clear() {
        itemList.clear()
        notifyDataSetChanged()
    }

    @CallSuper
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    public abstract class ViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public val context: Context = itemView.context

        public abstract fun bind(item: T)

        public open fun unbind() {}
    }
}
