package io.getstream.chat.android.core.poc.app.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.core.poc.R

class ChannelsListAdapter(private val data: List<Channel>) :
    RecyclerView.Adapter<ChannelsListAdapter.VH>() {

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

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textId = view.findViewById<TextView>(R.id.text_id)
        val textName = view.findViewById<TextView>(R.id.text_name)
    }
}