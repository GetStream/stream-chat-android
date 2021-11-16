package io.getstream.videosample.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.videosample.R

class ItemsAdapter : ListAdapter<String, ItemsAdapter.ItemViewHolder>(Diff()) {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(text: String) {
            itemView.findViewById<TextView>(R.id.itemText).text = text
        }
    }

    class Diff: DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.stream_ui_item_message_plain, parent, false)
            .let { ItemViewHolder(it) }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
