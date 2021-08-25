package io.getstream.chat.android.client.utils.internal.toggle.dialog

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.R

internal class ToggleAdapter(private val listener: ToggleSwitchListener) : RecyclerView.Adapter<ToggleItemViewHolder>() {
    private var toggles: List<Pair<String, Boolean>> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Pair<String, Boolean>>) {
        toggles = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToggleItemViewHolder {
        return LayoutInflater.from(parent.context).inflate(R.layout.stream_toggle_list_item, parent, false)
            .let(::ToggleItemViewHolder)
    }

    override fun onBindViewHolder(holder: ToggleItemViewHolder, position: Int) {
        holder.bindData(toggles[position], listener)
    }

    override fun getItemCount(): Int = toggles.size
}

internal fun interface ToggleSwitchListener {
    fun onSwitched(toggleName: String, isEnabled: Boolean)
}

internal class ToggleItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val label: TextView
        get() = itemView.findViewById(R.id.label)
    private val switch: SwitchCompat
        get() = itemView.findViewById(R.id.switcher)

    fun bindData(toggle: Pair<String, Boolean>, listener: ToggleSwitchListener) {
        label.text = toggle.first
        switch.isChecked = toggle.second
        switch.setOnCheckedChangeListener { _, isChecked -> listener.onSwitched(toggle.first, isChecked) }
    }
}
