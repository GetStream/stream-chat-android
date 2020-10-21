package io.getstream.chat.android.client.sample.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.sample.R

class ChannelsListAdapter(data: List<Channel>) :
    RecyclerView.Adapter<ChannelsListAdapter.VH>() {

    val data = mutableListOf<Channel>()

    init {
        this.data.addAll(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.channel_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val channel = data[position]
        holder.textId.text = channel.id
        holder.textName.text = channel.name
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    fun setOrUpdate(
        channels: List<Channel>
    ) {
        if (channels.isEmpty()) return
        if (data.isEmpty()) {
            data.addAll(channels)
            notifyItemInserted(0)
        } else {

            if (validateTheSame(data, channels)) {
                return
            }

            val sorted = mergeAndSort(data, channels)
            val diffResult = DiffUtil.calculateDiff(DiffCallback(data, sorted), true)
            data.clear()
            data.addAll(sorted)
            diffResult.dispatchUpdatesTo(this)
            validateDuplicates()
        }
    }

    private fun validateTheSame(ch1: List<Channel>, ch2: List<Channel>): Boolean {
        if (ch1.size == ch2.size) {
            var sameCounter = 0
            ch1.forEachIndexed { index, channel ->
                if (ch1[index].id == ch2[index].id) {
                    sameCounter++
                }
            }

            if (ch1.size == sameCounter) {
                println("same list!")
                return true
            }
        }
        return false
    }

    private fun validateDuplicates() {
        val ids = mutableSetOf<String>()
        data.forEach {
            if (ids.contains(it.id)) {
                println("duplicate: ${it.id}")
            } else {
                ids.add(it.id)
            }
        }
    }

    private fun mergeAndSort(
        current: List<Channel>,
        update: List<Channel>
    ): List<Channel> {
        val result: MutableList<Channel> = ArrayList(current)
        for (ch in update) {
            val idx = current.indexOfFirst { it.id == ch.id }

            if (idx != -1) {
                result[idx] = ch
            } else {
                result.add(ch)
            }
        }

        result.sortBy(Channel::updatedAt)
        return result
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textId: TextView = view.findViewById(R.id.text_id)
        val textName: TextView = view.findViewById(R.id.text_name)
    }
}
