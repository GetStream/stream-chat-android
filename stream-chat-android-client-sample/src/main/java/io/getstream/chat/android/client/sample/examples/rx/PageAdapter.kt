package io.getstream.chat.android.client.sample.examples.rx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.sample.common.Channel
import io.getstream.chat.android.client.sample.common.DiffCallback

class PageAdapter :
    RecyclerView.Adapter<PageAdapter.VH>() {

    val channels = mutableListOf<Channel>()
    val pages = mutableMapOf<Int, List<Channel>>()

    fun clear() {
        channels.clear()
        pages.clear()
        notifyDataSetChanged()
    }

    fun addPage(offset: Int, newPage: List<Channel>) {
        if (pages.containsKey(offset)) {
            // update page

            if (newPage.isEmpty()) {
                // page was removed completely
                val removedPage = pages.remove(offset)!!
                val sorted = removeAndSort(channels, removedPage)
                applyDiff(sorted)
            } else {
                // merge old and new pages
                val oldPage = pages[offset]
                pages[offset] = newPage

                val newChannels = ArrayList(channels)

                oldPage!!.forEach {
                    val toBeRemoved = it
                    newChannels.removeIf {
                        toBeRemoved.id == it.id
                    }
                }

                newChannels.addAll(newPage)
                newChannels.sortWith(java.util.Comparator { o1: Channel, o2: Channel -> (o1.updatedAt - o2.updatedAt) })
                applyDiff(newChannels)

                // setOrUpdate(newPage)
            }
        } else {
            // add new page
            pages[offset] = newPage
            setOrUpdate(newPage)
        }
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
        return channels.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val channel = channels[position]
        holder.textId.text = "id: ${channel.remoteId}"
        holder.textName.text = "name: ${channel.name}"
        holder.textUpdatedAt.text = "updated at: ${channel.updatedAt}"
    }

    private fun setOrUpdate(
        newChannels: List<Channel>
    ) {
        if (newChannels.isEmpty()) return
        if (channels.isEmpty()) {
            channels.addAll(newChannels)
            notifyItemInserted(0)
        } else {

            if (validateTheSame(channels, newChannels)) {
                return
            }

            val sorted = mergeAndSort(channels, newChannels)
            applyDiff(sorted)
        }
    }

    private fun applyDiff(new: List<Channel>) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(channels, new), true)
        channels.clear()
        channels.addAll(new)
        diffResult.dispatchUpdatesTo(this)
        validateDuplicates()
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

    private fun removeAndSort(
        current: List<Channel>,
        toRemove: List<Channel>
    ): List<Channel> {
        val result: MutableList<Channel> = ArrayList(current)
        for (rem in toRemove) {
            val id = rem.id
            val idx = result.indexOfFirst { it.id == id }
            if (idx != -1) result.removeAt(idx)
        }

        result.sortBy(Channel::updatedAt)
        return result
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
        channels.forEach {
            if (ids.contains(it.id)) {
                println("duplicate: ${it.id}")
            } else {
                ids.add(it.id)
            }
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textId: TextView = view.findViewById(R.id.text_id)
        val textName: TextView = view.findViewById(R.id.text_name)
        val textUpdatedAt: TextView = view.findViewById(R.id.text_updated_at)
    }
}
