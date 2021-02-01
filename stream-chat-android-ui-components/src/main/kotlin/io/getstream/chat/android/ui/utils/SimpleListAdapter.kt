package io.getstream.chat.android.ui.utils

import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView

internal abstract class SimpleListAdapter<T : Any, VH : SimpleListAdapter.ViewHolder<T>> : RecyclerView.Adapter<VH>() {
    var itemList: MutableList<T> = mutableListOf()

    final override fun getItemCount(): Int = itemList.size

    final override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(itemList[position])
    }

    fun setItems(items: List<T>) {
        this.itemList.clear()
        this.itemList.addAll(items)
        notifyDataSetChanged()
    }

    fun removeItem(item: T) {
        val index = itemList.indexOf(item)
        if (index != -1) {
            itemList.remove(item)
            notifyItemRemoved(index)
        }
    }

    fun clear() {
        itemList.clear()
        notifyDataSetChanged()
    }

    @CallSuper
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    abstract class ViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context

        abstract fun bind(item: T)

        open fun unbind() {}
    }
}
